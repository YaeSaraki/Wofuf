## 5 系统实现

### 5.1 用户管理模块

用户管理模块是Wofuf系统的基础认证子域，基于DDD分层架构实现，提供了用户注册、登录、登出、令牌刷新和账户删除等核心功能。该模块由用户子域（Wofuf-users）独立服务承载，拥有独立的MySQL数据库实例。

#### 5.1.1 用户注册

用户注册功能通过CreateUserController和CreateUserUseCase实现。用户提交邮箱、用户名和密码后，系统在应用层通过Value Object机制进行输入合法性校验：UserEmail使用正则表达式验证邮箱格式并自动转为小写；UserName要求3至50个字符且仅允许字母、数字、下划线和连字符；UserPassword要求6至100个字符，创建时通过BCrypt算法自动进行哈希加密。校验通过后，系统通过UserRepo查询数据库验证邮箱和用户名的唯一性，避免重复注册。验证全部通过后，系统调用User聚合根的工厂方法create()创建用户实体，其中isEmailVerified字段初始为false，isAdminUser字段初始为false。实体创建成功后，系统通过IDomainEvents接口发布UserCreated领域事件，并将用户实体持久化到数据库。整个流程采用Kotlin的Result模式统一封装成功与失败结果，错误类型包括EmailAlreadyExistsError、UsernameAlreadyExistsError等，最终通过ApiResponse统一返回给前端。

#### 5.1.2 用户登录

用户登录功能通过LoginController和LoginUseCase实现，集成了Spring Security认证框架。用户提交用户名和密码后，系统首先通过UserName和UserPassword两个Value Object进行输入格式校验，然后通过UserRepo查找用户实体。用户存在后，系统构造UsernamePasswordAuthenticationToken凭证，委托Spring Security的AuthenticationManager执行密码认证，AuthenticationManager内部通过UserDetailsService加载用户详情并进行BCrypt密码比对。认证通过后，系统调用UserAuthService的login()方法生成JWT访问令牌（AccessToken）和刷新令牌（RefreshToken），并更新用户实体上的令牌状态，同时发布UserLoggedIn领域事件。系统将令牌信息封装为LoginDto.Response返回给客户端，后续请求通过HTTP Header携带访问令牌完成身份验证。

#### 5.1.3 令牌管理与用户登出

令牌管理包括令牌刷新和用户登出两个功能。令牌刷新通过RefreshAccessTokenUseCase实现：系统接收客户端提交的刷新令牌，通过UserAuthService验证其有效性并生成新的令牌对，同时使旧刷新令牌失效，实现了令牌轮换（Token Rotation）机制。用户登出通过LogoutUseCase实现：系统验证用户身份后，调用UserAuthService将当前访问令牌加入Redis黑名单（数据库0），确保已登出的令牌无法继续使用，同时清除用户实体上存储的令牌信息。

#### 5.1.4 用户删除

用户删除功能通过DeleteUserController和DeleteUserUseCase实现，采用软删除策略。系统通过UserId值对象验证目标用户ID的有效性，查找用户实体后调用其实体方法delete()将isDeleted标记设为true，并发布UserDeleted领域事件。软删除机制保留了用户的历史数据，确保论坛中该用户发布的帖子和评论不会因账户删除而丢失作者信息。用户实体的UserDetails接口实现中，isAccountNonLocked()和isEnabled()方法均返回isDeleted的取反值，使得被删除的用户无法通过Spring Security认证。

#### 5.1.5 用户查询

用户查询包括按用户名查询和获取当前用户信息两个功能，分别通过GetUserByUsernameUseCase和GetCurrentUserUseCase实现。前者通过UserName值对象校验后查询数据库，返回用户的公开信息（用户名、邮箱、邮箱验证状态、管理员标识、最后登录时间）；后者通过请求头中的认证令牌识别当前用户身份并返回其详细信息。

### 5.2 玩家数据模块

玩家数据模块是Wofuf系统的核心数据子域，负责Minecraft游戏玩家数据的采集、存储和查询。该模块由玩家子域（Wofuf-players）独立服务承载，拥有独立的MySQL数据库实例和Redis缓存实例（数据库1）。

#### 5.2.1 玩家数据采集

玩家数据采集通过CollectPlayerDataScheduler和CollectPlayerDataUseCase协同实现，是系统与Minecraft服务器数据同步的核心机制。CollectPlayerDataScheduler配置了两个定时任务：采集任务（默认每60秒执行一次）通过PluginApiClient调用Minecraft服务器插件API获取当前在线玩家列表，对每个在线玩家依次获取统计数据（PlayerStatistic）、成就信息（PlayerAdvancement）和皮肤数据（PlayerSkin）；队列清理任务（默认每600秒执行一次）从内存采集队列中移除最早的记录。系统采用双层防重复机制：内存层的ConcurrentHashMap记录近期已采集的玩家UUID，Redis层的PlayerCollectCooldownCache以可配置的TTL存储采集冷却标记，两层同时校验以避免对同一玩家的频繁采集。采集到的原始数据通过值对象封装后传入CollectPlayerDataUseCase，该用例根据玩家UUID查询数据库判断玩家是否存在：不存在则通过Player.create()创建新实体并持久化，已存在则调用实体的updateProps()方法更新全部属性，实现了upsert语义。

#### 5.2.2 玩家信息查询

玩家信息查询通过GetPlayerController和GetPlayerUseCase实现，支持按玩家名称和UUID两种方式查询。系统根据输入字符串长度（≥36判定为UUID，<36判定为名称）自动选择查询路径，查询到玩家实体后通过GetPlayerDtoMap映射为DTO返回，包含玩家名称、UUID、首次登录时间、最后登录时间和总游戏时长等基本信息。对于不存在的玩家，系统返回GetPlayerError。

#### 5.2.3 玩家统计数据查询

玩家统计数据查询通过GetPlayerStatisticsController实现。用户通过玩家UUID查询后，系统返回存储在statistics_json字段中的完整统计数据，该字段以JSON格式存储Map<String, PlayerStatistic>结构。Controller层支持通过category、categories、key、keys等查询参数对统计数据进行筛选过滤，允许客户端按需获取特定类型的数据（如战斗统计、采集统计等），减少网络传输量。

#### 5.2.4 玩家成就与皮肤查询

玩家成就查询通过GetPlayerAdvancementsController实现，返回存储在advancements_json字段中的成就数据（Map<String, PlayerAdvancement>结构）。Controller层通过includeRecipes查询参数控制是否包含配方类成就，默认过滤掉以减少响应数据量。玩家皮肤查询通过GetPlayerSkinController实现，返回玩家的皮肤类型、皮肤URL和披风URL。

#### 5.2.5 玩家搜索与发现

玩家搜索通过SearchPlayersController和SearchPlayersUseCase实现，支持按名称的模糊搜索和UUID前缀匹配。系统对查询关键词进行trim处理后调用PlayerRepo的searchByQuery方法，该方法使用JPA的LIKE查询实现大小写不敏感的名称匹配和UUID前缀匹配，结果按玩家名称升序排列。搜索结果数量通过limit参数控制（默认20，最大50），返回玩家的简要信息（ID、名称、最后登录时间）。此外，系统还提供获取随机玩家（GetPlayerRandomController，支持指定数量）和获取昨日在线玩家（GetPlayerYesterdayOnlineController）功能，后者使用Redis缓存昨日在线玩家列表，TTL为24小时，并通过定时任务在每日零点预热缓存。

### 5.3 论坛模块

论坛模块是Wofuf系统的社区交互子域，提供了成员管理、帖子管理、评论系统和投票机制等功能。该模块由论坛子域（Wofuf-forum）独立服务承载，拥有独立的MySQL数据库实例。

#### 5.3.1 论坛成员管理

论坛成员是用户在论坛中的身份实体，通过CreateMemberUseCase创建。创建成员时需提供用户ID、玩家ID、昵称和验证码，系统首先通过HashVerifyUtil验证请求中的验证码与服务器端密钥生成的HMAC签名是否匹配，确保请求来自合法客户端（防止伪造的跨服务绑定请求）。验证通过后，系统通过UserId、PlayerId和NickName三个值对象对输入进行合法性校验，其中NickName要求3至50个字符且仅允许字母、数字、下划线和连字符。校验通过后创建Member聚合根并持久化，同时发布MemberCreated领域事件。该机制将用户账户、Minecraft玩家身份和论坛成员身份进行三方绑定，确保论坛交互的可追溯性。系统还提供获取当前成员（GetCurrentMemberUseCase）和按用户名查询成员（GetMemberByUserNameUseCase）功能。

#### 5.3.2 帖子管理

帖子管理是论坛的核心功能，涵盖了帖子的创建、查询、编辑和删除。

**创建帖子**通过CreatePostUseCase实现。用户提交标题、内容（Markdown格式）、帖子类型（TEXT或LINK）和可选链接后，系统验证用户ID和标题的非空性，通过MemberRepo查找用户的论坛成员身份。验证通过后，系统创建PostTitle值对象（2至100字符）并基于标题自动生成PostSlug（格式为"标题简化文本-7位随机数字"），作为帖子的URL友好标识。系统通过MarkdownImageUtils校验帖子正文中引用的图片数量是否超出限制，防止滥用存储资源。最后创建Post聚合根并持久化，初始投票分数为0，分类默认为DISCUSSION。

**查询帖子**通过GetPostBySlugUseCase实现，支持按slug标识获取帖子详情。查询时系统同时通过PostVoteDomainService获取当前用户对该帖子的投票状态，并通过postRepo统计评论总数，返回包含投票状态和评论数量的完整帖子信息。

**编辑帖子**通过EditPostUseCase实现，允许作者修改帖子的标题、内容和链接。系统要求至少提供一个待更新字段，对每个字段分别创建对应的值对象进行合法性校验（内容字段同样包含图片数量限制检查），然后调用帖子实体的edit()方法执行原子性更新。

**删除帖子**通过DeletePostUseCase实现，对帖子ID进行有效性验证和存在性检查后执行物理删除。

**帖子列表**通过GetPopularPostsUseCase和GetRecentPostsUseCase实现，分别按投票分数和发布时间排序，支持分页查询和按分类（DISCUSSION、SHARE、QUESTION、ANNOUNCEMENT）筛选。为避免N+1查询问题，系统通过PostVoteDomainService.getVoteStatuses()批量获取当前用户对所有帖子的投票状态。

#### 5.3.3 评论系统

评论系统支持对帖子的顶层回复和对已有评论的嵌套回复，形成树状讨论结构。

**回复帖子**通过ReplyToPostUseCase实现，支持通过帖子ID或帖子slug两种方式定位目标帖子。系统验证评论内容（2至20000字符）和用户的论坛成员身份后，创建parentCommentId为null的Comment实体并持久化。

**回复评论**通过ReplyToCommentUseCase实现，在回复帖子的基础上额外指定父评论ID（parentCommentId），系统验证父评论的存在性后创建带有parentCommentId的Comment实体，实现嵌套回复。

**查询评论**包括按评论ID查询（GetCommentByCommentIdUseCase）和按帖子slug查询（GetCommentByPostSlugUseCase）两种方式。后者返回该帖子下所有评论列表，并通过CommentVoteDomainService.getVoteStatuses()批量获取投票状态，避免N+1查询。

#### 5.3.4 投票机制

投票机制是论坛互动的核心功能，支持对帖子和评论的点赞和点踩操作，通过领域服务封装复杂的跨聚合投票逻辑。

投票功能由PostVoteDomainService和CommentVoteDomainService两个领域服务实现，二者遵循相同的设计模式。投票采用Toggle（切换）机制：若用户未投票则创建新投票记录；若已点赞则再次点赞将取消投票（删除投票记录）；若已点踩则切换为点赞（更新投票记录类型），反之亦然。投票完成后，系统重新统计upvote和downvote数量，计算新的分数（points = upvotes - downvotes），并更新目标实体（帖子或评论）的分数字段。

取消投票功能（UnvotePostUseCase）允许用户直接撤销对帖子的投票，系统查找并删除投票记录后重新计算分数。投票数据通过独立的PostVote和CommentVote聚合存储，与帖子和评论实体分离，遵循DDD中每个聚合独立管理的原则。

#### 5.3.5 图片上传

图片上传功能通过UploadImageUseCase实现。用户通过multipart/form-data格式上传图片文件，系统验证文件的非空性后委托ImageStorageService执行存储操作。系统支持基于MD5哈希的文件去重机制：上传时计算文件的MD5值，若已存在相同MD5的文件则直接返回已有URL，避免重复存储。上传成功后，系统自动生成Markdown格式的图片引用文本（`![filename](url)`），方便用户在帖子或评论中直接插入。系统对文件大小和类型进行限制，上传失败时返回具体的错误信息（文件过大、文件类型不支持等）。

### 5.4 数据同步模块

数据同步模块是连接Minecraft游戏服务器与Wofuf系统的桥梁，通过定时任务机制实现玩家数据的自动采集和同步。

#### 5.4.1 采集调度机制

采集调度由CollectPlayerDataScheduler组件实现，基于Spring的@Scheduled注解配置两个定时任务。采集任务以可配置的间隔（默认60秒）通过PluginApiClient调用Minecraft服务器插件API获取当前在线玩家列表，对列表中每个玩家依次采集基本信息、统计数据、成就信息和皮肤数据。PluginApiClient封装了与游戏服务器插件REST API的HTTP通信，处理了请求超时和连接异常等边界情况。

#### 5.4.2 防重复采集机制

为避免对同一玩家的频繁数据采集造成服务器插件API的压力，系统实现了双层防重复机制。内存层使用ConcurrentHashMap维护一个近期已采集玩家的UUID集合，每次采集前检查目标玩家是否在集合中；Redis层使用PlayerCollectCooldownCache以"players:collect:cooldown:{playerName}"为键存储冷却标记，TTL可通过配置参数调整。队列清理定时任务定期从内存集合中移除最早的记录，实现冷却窗口的滚动更新。两层机制协同工作，确保每个玩家在冷却时间窗口内仅被采集一次。

#### 5.4.3 数据处理与持久化

采集到的原始数据通过CollectPlayerDataUseCase进行领域对象封装和持久化处理。统计数据通过PlayerStatistic.create()将API返回的属性映射为值对象，成就信息通过PlayerAdvancement.create()进行同样的封装，皮肤数据通过PlayerSkin.create()提取类型、皮肤URL和披风URL。若统计或成就数据为空，系统跳过该玩家的采集并记录警告日志。封装完成后，系统根据玩家UUID查询数据库判断实体是否存在：新玩家通过Player.create()工厂方法创建实体并持久化，已有玩家通过updateProps()方法更新全部属性（包括updateTime时间戳），确保本地数据与游戏服务器保持同步。
