## 4.2 数据库设计

Wofuf系统采用微服务架构，其数据库设计遵循领域驱动设计（DDD）的限界上下文隔离原则，采用数据库-per-service模式。每个业务微服务拥有独立的数据库实例，服务间不直接共享数据库，而是通过REST API和领域事件进行数据交互。系统共包含3个独立的MySQL数据库和1个共享Redis缓存实例，分别服务于用户管理、玩家数据管理和论坛管理三个业务子域。

### 4.2.1 数据库总览

| 数据库 | 对应服务 | 职责 |
|--------|---------|------|
| users_db | Wofuf-users | 用户账户管理、认证令牌存储 |
| players_db | Wofuf-players | Minecraft玩家数据存储与查询 |
| forum_db | Wofuf-forum | 论坛成员、帖子、评论、投票管理 |
| Redis（DB 0） | Wofuf-users | JWT令牌黑名单、认证会话管理 |
| Redis（DB 1） | Wofuf-players | 玩家数据缓存、采集冷却控制 |

### 4.2.2 E-R图

Wofuf系统的数据模型按限界上下文划分，各子域内部的实体关系如下。

#### 4.2.2.1 用户子域实体（users_db）

用户子域仅包含一个核心实体，结构较为精简。

**用户（users）**：系统的核心认证实体，存储用户的基本信息和认证状态。主要字段包括：id（主键，UUID）、email（唯一约束）、username（唯一约束）、password（BCrypt哈希）、is_email_verified、is_admin_user、is_deleted（软删除标记）、access_token、refresh_token、last_login、create_time、update_time。

#### 4.2.2.2 玩家子域实体（players_db）

玩家子域仅包含一个核心实体，游戏统计数据和成就信息以JSON格式嵌入存储。

**玩家（players）**：Minecraft玩家的数据实体，存储从游戏服务器采集的玩家信息。主要字段包括：player_uuid（主键，UUID）、player_name（唯一约束）、first_login、last_login、total_playtime_seconds、update_time、statistics_json（JSON，存储Map<String, PlayerStatistic>）、advancements_json（JSON，存储Map<String, PlayerAdvancement>）、skin_type、skin_url、cape_url。

该实体通过CollectPlayerDataScheduler定时从Minecraft服务器插件API采集数据，采用upsert策略（存在则更新，不存在则创建）。

#### 4.2.2.3 论坛子域实体（forum_db）

论坛子域包含5个实体，关系较为复杂。

**论坛成员（members）**：用户在论坛中的身份实体，将用户账户与Minecraft玩家身份进行绑定。主要字段包括：id（主键，UUID）、user_id（外键，关联用户子域的用户ID）、player_id（外键，关联玩家子域的玩家UUID）、nick_name（唯一约束）、reputation（声望值）。user_id与player_id的组合构成业务层面的唯一约束，确保一个用户只能绑定一个玩家身份。该实体通过HMAC签名验证机制创建，确保绑定的安全性。

**帖子（posts）**：论坛的核心内容实体。主要字段包括：id（主键，UUID）、member_id（外键，关联members.id）、slug（唯一约束，由标题自动生成的URL友好标识）、title、type（枚举：TEXT、LINK）、category（枚举：DISCUSSION、SHARE、QUESTION、ANNOUNCEMENT，默认DISCUSSION）、text（Markdown格式，最大50000字符）、link（可选，链接类型帖子使用）、total_num_comments、points（投票分数）、date_time_posted。slug字段通过PostTitle生成，格式为"标题简化-7位随机数字"，用于构建SEO友好的URL。

**评论（comments）**：帖子下的评论实体，支持嵌套回复结构。主要字段包括：id（主键，UUID）、member_id（外键，关联members.id）、post_id（外键，关联posts.id）、parent_comment_id（自引用外键，关联comments.id，可为null，null表示顶层评论）、text（最大20000字符）、points（投票分数）。

**帖子投票（post_votes）**：用户对帖子的投票记录。主要字段包括：id（主键，UUID）、post_id（外键，关联posts.id）、member_id（外键，关联members.id）、vote_type（枚举：UPVOTE、DOWNVOTE）。post_id与member_id的组合构成唯一约束，确保每个成员对每个帖子只能有一条投票记录。投票采用Toggle机制：重复相同投票操作将取消投票。

**评论投票（comment_votes）**：用户对评论的投票记录，结构同帖子投票。主要字段包括：id（主键，UUID）、comment_id（外键，关联comments.id）、member_id（外键，关联members.id）、vote_type（枚举：UPVOTE、DOWNVOTE）。comment_id与member_id的组合构成唯一约束。

#### 4.2.2.4 实体关系描述

**论坛成员与帖子的关系**：一对多（One-to-Many）。一个论坛成员可以发布多个帖子，一个帖子只能属于一个论坛成员。posts表的member_id字段为外键，引用members表的id字段。

**论坛成员与评论的关系**：一对多（One-to-Many）。一个论坛成员可以发布多条评论，一条评论只能属于一个论坛成员。comments表的member_id字段为外键，引用members表的id字段。

**帖子与评论的关系**：一对多（One-to-Many）。一个帖子可以包含多条评论，一条评论只能属于一个帖子。comments表的post_id字段为外键，引用posts表的id字段。帖子的total_num_comments字段记录评论总数。

**评论与子评论的关系**：一对多（One-to-Many，自引用）。一条评论可以包含多条子评论，支持多级嵌套回复结构。comments表的parent_comment_id字段为自引用外键，引用自身表的id字段，null表示该评论为帖子的顶层评论。

**帖子与帖子投票的关系**：一对多（One-to-Many）。一个帖子可以拥有多条投票记录，通过post_votes表实现。投票分数（points）由upvote数量减去downvote数量计算得出。

**评论与评论投票的关系**：一对多（One-to-Many）。一条评论可以拥有多条投票记录，通过comment_votes表实现。

**论坛成员与用户/玩家的跨域关联**：论坛子域的members实体通过user_id和player_id字段分别引用用户子域和玩家子域的实体。由于采用数据库-per-service模式，这些跨域引用不使用数据库外键约束，而是在应用层通过REST API调用进行数据一致性校验。

### 4.2.3 数据库设计特点

1. **数据库隔离**：各微服务拥有独立数据库，避免了单点故障和跨服务的数据耦合，每个服务可以独立进行数据库的扩缩容和schema演进。

2. **JSON嵌套存储**：玩家的统计数据和成就信息以JSON格式存储在players表中，避免了大量的关联表和JOIN操作，适合这类读多写少、数据结构相对固定的场景。

3. **软删除机制**：用户表采用is_deleted字段实现软删除，保留了用户的历史数据，确保论坛帖子和评论的作者信息不会因账户删除而丢失。

4. **最终一致性**：由于服务间不共享数据库，跨域数据（如用户删除后清理论坛成员）的一致性通过领域事件和API调用实现最终一致性，而非数据库事务的强一致性。

5. **Redis缓存策略**：使用独立的Redis数据库实例分别缓存用户认证信息和玩家数据，通过TTL机制管理缓存生命周期，减少数据库访问压力。
