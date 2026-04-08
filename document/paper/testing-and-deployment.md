## 6 系统测试与部署

### 6.1 系统测试

Wofuf系统的测试基于Kotlin测试框架和JUnit 5平台构建，采用分层测试策略，覆盖从基础设施层到应用层的各级组件。

#### 6.1.1 用户服务模块测试

用户服务模块的测试聚焦于密码加密机制的正确性验证。系统通过两个单元测试确保密码处理的安全性：第一个测试验证了自定义PasswordEncoder组件的编码行为，确认明文密码经BCrypt算法加密后生成不可逆的哈希值，且加密结果与原始密码不相等；第二个测试直接使用Spring Security提供的BCryptPasswordEncoder验证密码匹配逻辑，确保加密后的密码能够正确匹配原始明文。测试断言包括加密结果非空（assertNotNull）、加密前后值不等（assertNotEquals）以及加密后可正确匹配（assertTrue）。

测试配置通过Gradle构建脚本限定测试范围（`include("dev/saraki/wofuf/modules/users/infra/auth/**")`），并设置`failOnNoDiscoveredTests = false`避免无测试时构建失败。测试运行基于JUnit Platform，使用`useJUnitPlatform()`配置。

#### 6.1.2 玩家服务模块测试

玩家服务模块作为系统的核心数据处理子域，其测试围绕数据采集的完整性和查询接口的正确性展开。

数据采集测试重点验证了CollectPlayerDataUseCase的upsert逻辑：对于新玩家，验证系统能正确创建Player聚合根并通过Value Object校验玩家名称和UUID的合法性，持久化到数据库后返回完整的玩家实体；对于已有玩家，验证系统通过updateProps()方法正确更新所有属性（包括基本信息、统计数据、成就信息和皮肤数据），并更新updateTime时间戳。测试还覆盖了异常场景，包括无效UUID格式的PlayerId创建失败、空玩家名称的校验失败等错误路径。

查询接口测试验证了GetPlayerUseCase的双路径查询逻辑：输入长度≥36时按UUID查询，<36时按名称查询。同时验证了SearchPlayersUseCase的输入校验规则（查询关键词非空、limit参数为正整数且不超过50）以及GetPlayerSkinUseCase的数据提取逻辑。

#### 6.1.3 论坛服务模块测试

论坛服务模块的测试覆盖了帖子管理、评论系统和投票机制等核心功能。

帖子管理测试验证了CreatePostUseCase的完整校验链：用户ID和标题的非空性校验、论坛成员身份的存在性验证、PostTitle的长度限制（2至100字符）、PostType枚举的有效性（TEXT/LINK）、PostText的长度限制（2至50000字符）以及MarkdownImageUtils对图片数量的限制校验。编辑帖子测试验证了EditPostUseCase的"至少提供一个更新字段"约束和PostLink值对象的合法性校验。

评论系统测试验证了ReplyToPostUseCase和ReplyToCommentUseCase的创建逻辑，包括CommentText的长度限制（2至20000字符）、parentCommentId的设置（顶层评论为null、嵌套评论指向父评论ID）以及帖子定位的双模式支持（通过ID或slug）。

投票机制测试验证了PostVoteDomainService的Toggle逻辑：首次点赞创建UPVOTE记录，重复点赞删除投票记录（取消），从点赞切换为点踩则更新投票类型。测试还验证了投票后分数重新计算的准确性（points = upvotes - downvotes）。

#### 6.1.4 前端应用模块测试

前端应用基于Vue 3 + TypeScript + Vite技术栈构建，使用Vite的构建工具链进行类型检查和代码质量验证。项目配置了ESLint代码检查规则和Prettier代码格式化工具，通过`npm run lint`和`npm run format`命令自动检测和修复代码风格问题。构建流程通过`npm run build`执行类型检查（vue-tsc）和产物构建（Vite）两个阶段，确保TypeScript类型安全和构建产物的正确性。

### 6.2 系统部署

#### 6.2.1 容器化部署架构

系统采用容器化部署架构，通过Docker Compose编排基础设施服务，确保开发环境的一致性和可移植性。部署架构将基础设施划分为独立的容器服务，包括MySQL数据库、Redis缓存、Apache Kafka消息队列和MinIO对象存储，各容器通过自定义桥接网络（woffo-network）进行通信。

系统后端采用多模块Gradle单体构建方式，各业务子域（users、players、forum）作为独立的Spring Boot应用运行，通过Spring Cloud Gateway实现API网关路由。前端应用使用Vite开发服务器进行本地开发，生产环境通过构建生成静态资源部署。

#### 6.2.2 基础设施容器配置

Docker Compose配置文件（docker-compose.yml）定义了以下基础设施服务：

**MySQL数据库**：使用MySQL Latest镜像，暴露3307端口（映射容器内部3306端口），配置了独立的数据库名称（Woffo_db）、用户名（Woffo_db_user）和密码，供各业务子域连接使用。系统采用数据库-per-service模式，各子域在同一MySQL实例中创建独立的数据库（schema），实现逻辑隔离。

**Redis缓存**：使用Redis Latest镜像，暴露6380端口（映射容器内部6379端口），配置了多个数据库实例用于不同用途：数据库0用于用户模块的JWT令牌黑名单管理，数据库1用于玩家模块的数据缓存和采集冷却控制。

**Apache Kafka**：使用Apache Kafka Latest镜像（KRaft模式，无需ZooKeeper），暴露9092端口供客户端访问和9093端口用于控制器内部通信。配置了单节点集群模式（broker + controller），设置了固定集群ID和KRaft协议相关参数。数据通过命名卷（kafka-data）持久化存储，确保消息不丢失。

**MinIO对象存储**：使用MinIO Latest镜像，暴露9000端口（API）和9001端口（Console管理界面），用于论坛模块的图片上传和存储功能。数据通过命名卷（minio-data）持久化。

#### 6.2.3 后端服务构建

后端服务基于Spring Boot 4.0.1 + Kotlin 2.2构建，Java工具链版本为17。构建系统采用Gradle多模块结构，根项目统一管理依赖版本（通过gradle/libs.versions.toml），各子模块独立声明依赖关系。核心依赖包括Spring Boot Starter（Web、Data JPA、Data Redis、Security）、Spring Cloud（Gateway、Eureka、Kafka Stream）、Eventuate Tram（领域事件框架）和JJWT（JWT令牌处理）。

各业务模块通过Gradle的bootRun任务独立启动，也可通过`./gradlew :module-name:build`单独构建。测试执行配置了JUnit Platform，并限定各模块的测试范围以控制构建时间。SpringDoc OpenAPI（版本3.0.1）提供了API文档支持，各服务启动后可通过Swagger UI查阅和测试API接口。

#### 6.2.4 前端应用构建

前端应用基于Vue 3.5 + TypeScript 5.9 + Vite 7.3构建，要求Node.js 20.19+版本。UI框架采用PrimeVue 4.5组件库配合Tailwind CSS 4.1，支持响应式布局。构建流程通过`npm run build`执行：首先运行vue-tsc进行TypeScript类型检查，确保类型安全；然后通过Vite进行生产环境构建，生成优化后的静态资源（包括HTML、CSS、JavaScript）。构建产物可部署到Nginx等静态文件服务器，或通过Vite的preview命令进行本地预览。

#### 6.2.5 部署流程与验证

本地开发环境的部署流程如下：首先通过`docker-compose up -d`命令启动基础设施容器（MySQL、Redis、Kafka、MinIO），等待所有容器就绪后，依次启动各业务子域服务（可通过IDE或Gradle bootRun），最后通过`npm run dev`启动前端开发服务器。Docker Compose的容器依赖管理确保了MySQL和Redis在应用服务启动前完成初始化。

部署验证包括以下步骤：通过`docker ps`命令确认所有基础设施容器正常运行；通过访问各子域的健康检查端点验证后端服务状态；通过Swagger UI查阅API文档并测试核心接口；通过浏览器访问前端应用验证页面渲染和API交互功能；通过查看各服务的日志输出排查启动异常或运行时错误。
