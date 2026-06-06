package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 数据填充脚本 - 用于快速填充校园二手交易平台测试数据
 * 通过 SEED_DATA 环境变量控制是否执行
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Random RAND = new Random();
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private Integer adminId;

    @Override
    public void run(String... args) {
        if (!"true".equals(System.getenv("SEED_DATA"))) {
            System.out.println("[DataSeeder] 跳过数据填充（SEED_DATA 环境变量未设置）");
            return;
        }
        System.out.println("[DataSeeder] ========================================");
        System.out.println("[DataSeeder] 开始填充测试数据...");
        
        try {
            step1_clearTables();
            List<String> userIds = step2_createUsers();
            Long adminId = step3_createAdmin();
            List<Integer> goodsIds = step4_createGoods(userIds);
            step5_createComments(userIds, goodsIds);
            
            System.out.println("[DataSeeder] ========================================");
            System.out.println("[DataSeeder] ✅ 数据填充完成！");
            System.out.println("[DataSeeder] 普通用户数: " + userIds.size());
            System.out.println("[DataSeeder] 管理员: admin (ID: " + adminId + ")");
            System.out.println("[DataSeeder] 商品数: " + goodsIds.size());
            System.out.println("[DataSeeder] 管理员密码: admin / 123456");
            System.out.println("[DataSeeder] ========================================");
        } catch (Exception e) {
            System.err.println("[DataSeeder] ❌ 数据填充失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== 阶段1: 清空现有数据 ==========
    private void step1_clearTables() {
        System.out.println("[DataSeeder] 步骤1: 清空现有数据...");
        String[] tables = {
            "comments", "favorite", "orders", "review", "chat_messages",
            "message_record", "bargain", "bargain_record", "exchange", "goods"
        };
        for (String table : tables) {
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
                jdbcTemplate.execute("TRUNCATE TABLE `" + table + "`");
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
                System.out.println("  - 清空表: " + table);
            } catch (Exception e) {
                System.out.println("  - 清空表失败(可能不存在): " + table);
            }
        }
        // 重置自增
        try {
            jdbcTemplate.execute("ALTER TABLE `user` AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE `goods` AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE `comments` AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE `orders` AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE `review` AUTO_INCREMENT = 1");
        } catch (Exception e) {
            System.out.println("  - 重置自增ID失败: " + e.getMessage());
        }
        System.out.println("[DataSeeder] 步骤1完成");
    }

    // ========== 阶段2: 创建10个普通用户 ==========
    private List<String> step2_createUsers() {
        System.out.println("[DataSeeder] 步骤2: 创建10个用户账号...");
        List<String> userIds = new ArrayList<>();
        
        String[] names = {
            "小陈同学", "李同学", "张同学", "王同学", "刘同学",
            "杨同学", "赵同学", "黄同学", "周同学", "吴同学"
        };
        String[] colleges = {
            "计算机学院", "经济管理学院", "外国语学院", "机械工程学院", "土木工程学院",
            "艺术学院", "文学院", "法学院", "体育学院", "医学院"
        };
        String[] majors = {
            "软件工程", "会计学", "英语", "机械设计", "土木工程",
            "视觉传达", "汉语言文学", "法学", "体育教育", "临床医学"
        };
        
        String sql = "INSERT INTO `user` (`username`, `password`, `name`, `avatar`, `email`, `college`, `student_id`, `phone`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        for (int i = 0; i < 10; i++) {
            String username = "user_" + String.format("%02d", i + 1);
            String name = names[i];
            String college = colleges[i];
            String major = majors[i];
            String studentId = "2023" + String.format("%03d", RAND.nextInt(900) + 100);
            String phone = "1" + (RAND.nextInt(9) + 1) + String.format("%09d", RAND.nextInt(1000000000));
            String hashedPassword = BCRYPT.encode("123456");
            String avatar = "https://api.dicebear.com/7.x/adventurer/svg?seed=" + username;
            String email = username + "@campus.edu";
            
            jdbcTemplate.update(sql, username, hashedPassword, name, avatar, email, college, studentId, phone);
            userIds.add(username);
            System.out.println("  - 创建用户: " + username + " (" + name + ", " + college + ")");
        }
        
        System.out.println("[DataSeeder] 步骤2完成");
        return userIds;
    }

    // ========== 阶段3: 创建管理员 ==========
    private Long step3_createAdmin() {
        System.out.println("[DataSeeder] 步骤3: 创建管理员账户...");
        
        // 先检查是否已存在
        List<Integer> existingIds = jdbcTemplate.queryForList("SELECT id FROM `user` WHERE username = 'admin'", Integer.class);
        Integer existingId = existingIds.isEmpty() ? null : existingIds.get(0);
        if (existingId != null) {
            System.out.println("  - 管理员已存在 (ID: " + existingId + ")");
            System.out.println("[DataSeeder] 步骤3完成");
            return existingId.longValue();
        }
        
        String sql = "INSERT INTO `user` (`username`, `password`, `name`, `email`, `role`, `status`) VALUES (?, ?, ?, ?, 2, 0)";
        jdbcTemplate.update(sql, "admin", BCRYPT.encode("123456"), "系统管理员", "admin@campus.com");
        List<Integer> adminIds = jdbcTemplate.queryForList("SELECT id FROM `user` WHERE username = 'admin'", Integer.class);
        adminId = adminIds.isEmpty() ? null : adminIds.get(0);
        
        System.out.println("  - 管理员: admin (ID: " + adminId + ")");
        System.out.println("[DataSeeder] 步骤3完成");
        return adminId.longValue();
    }

    // ========== 阶段4: 生成商品 ==========
    private List<Integer> step4_createGoods(List<String> userIds) {
        System.out.println("[DataSeeder] 步骤4: 生成商品数据...");
        List<Integer> goodsIds = new ArrayList<>();
        
        // 定义商品数据
        String[][] products = {
            // 数码产品
            {"出MacBook Air M1 2020款", "8+256G配置，成色很新，仅使用半年，因换Pro而出。配件齐全包括原装充电器和包装盒。", "2599.00", "数码", "手机/笔记本", "9"},
            {"iPad Mini 5 64G WiFi版", "95成新，无划痕无磕碰，一直贴膜戴套使用。屏幕完美，电池健康98%。", "1299.00", "数码", "平板", "9"},
            {"Sony WH-1000XM4 降噪耳机", "黑色，买了两个月用不习惯头戴式，降噪效果确实好。盒说全。", "899.00", "数码", "耳机", "9"},
            {"Keychron K2 机械键盘", "RGB背光，红轴，几乎全新，只打了一个月游戏就闲置了。", "299.00", "数码", "键盘", "9"},
            {"Switch OLED 白色版", "带两个游戏《塞尔达》《马里奥赛车》，九成新，手柄无漂移。", "1599.00", "数码", "游戏机", "9"},
            {"华为FreeBuds Pro 2", "雅川青配色，降噪很强，成色新，用了一个学期。", "399.00", "数码", "耳机", "8"},
            
            // 书籍
            {"考研数学全资料（张宇+李永乐）", "数学一全套，包括复习全书、1000题、660题、历年真题。书很新，有少量笔记。", "45.00", "书籍", "考研资料", "8"},
            {"C primer plus 第六版 中文版", "计算机经典教材，9成新，内有部分学习笔记但不多，适合初学者。", "35.00", "书籍", "教材", "9"},
            {"哈利波特全集 套装1-7", "人民文学出版社版本，九成新，轻微翻阅痕迹，收藏级。", "120.00", "书籍", "小说", "9"},
            {"高等数学 同济第八版 上下册", "全新！学长留下的，没打开过，配套习题册也在。", "25.00", "书籍", "教材", "10"},
            {"考研英语真题（黄皮书）", "张剑黄皮书，近20年真题，有笔记但很清晰，适合下一届学弟学妹。", "30.00", "书籍", "考研资料", "8"},
            
            // 服装
            {"Uniqlo 优衣库 UQ 联名卫衣", "M码，黑色，穿了一次小了出不掉，吊牌还在。", "79.00", "服装", "卫衣", "10"},
            {"Nike Air Force 1 白鞋", "37码，穿了一个月，鞋底磨损很小，非常百搭。", "280.00", "服装", "鞋类", "9"},
            {"ZARA 连衣裙 碎花", "S码，M码也能穿，夏天穿过两次，清洗过了。", "65.00", "服装", "连衣裙", "9"},
            
            // 生活用品
            {"小米台灯 Pro", "用了三个月，光线柔和不刺眼，适合宿舍用。带USB充电口。", "59.00", "生活用品", "台灯", "9"},
            {"收纳箱 透明大号 两个", "塑料加厚材质，60L容量，搬家带不走出。", "30.00", "生活用品", "收纳", "8"},
            {"小风扇 手持USB充电", "迷你便携，续航4小时，夏天神器。新的没用过，朋友送的。", "15.00", "生活用品", "小风扇", "10"},
            
            // 运动器材
            {"YONEX 尤尼克斯羽毛球拍", "天斧AX7PRO，已穿线，用了一个月手上有茧打不了。", "380.00", "运动", "羽毛球拍", "9"},
            {"斯伯丁篮球 7号", "标准比赛用球，室内室外通用，用了大概一学期。", "65.00", "运动", "篮球", "8"},
            {"瑜伽垫 加厚15mm", "粉色，NBR材质防滑，带收纳带，买重了用不上。", "35.00", "运动", "瑜伽", "9"},
            
            // 美妆
            {"MAC 子弹头口红 #Diva", "深红色，已空管，全新未拆封的是 #Chili。", "99.00", "美妆", "口红", "10"},
            {"欧莱雅面膜 补水面膜 20片", "全新未拆，室友送的太多了用不完。", "59.00", "美妆", "面膜", "10"},
            
            // 家居
            {"宿舍小桌子 折叠升降", "木质桌面，可调节高度，宿舍神器！搬家带不走。", "45.00", "家居", "小桌子", "9"},
            {"迷你小冰箱 45L", "用了半年制冷很好，静音不吵，适合放宿舍放饮料。", "180.00", "家居", "小冰箱", "8"},
            {"四件套 纯棉 1.2m床", "纯棉材质，灰色系，洗过几次但没起球，很柔软。", "55.00", "家居", "四件套", "8"},
        };
        
        String sql = "INSERT INTO `goods` (`user_id`, `title`, `description`, `price`, `image_url`, `category_name`, `goods_condition`, `view_count`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        for (int i = 0; i < products.length; i++) {
            String[] p = products[i];
            String title = p[0];
            String desc = p[1];
            String price = p[2];
            String category = p[3];
            String subCategory = p[4];
            String condition = p[5];
            
            // 先创建 username -> id 映射
            java.util.Map<String, Integer> usernameToId = new java.util.HashMap<>();
            for (String uname : userIds) {
                Integer id = jdbcTemplate.queryForObject(
                    "SELECT id FROM `user` WHERE username = ?", Integer.class, uname);
                usernameToId.put(uname, id);
            }

            // 随机分配用户 - 使用ID
            String randomUsername = userIds.get(RAND.nextInt(userIds.size()));
            Long userId = Long.valueOf(usernameToId.get(randomUsername));

            // 生成图片URL
            String imageUrl = "https://picsum.photos/seed/" + (i + 100) + "/400/400";
            
            // 随机浏览量
            int viewCount = RAND.nextInt(200) + 10;
            
            // 随机生成时间（过去30天内）
            LocalDateTime createTime = LocalDateTime.now().minusDays(RAND.nextInt(30));
            
            jdbcTemplate.update(sql, userId, title, desc, new BigDecimal(price), imageUrl, subCategory, Integer.parseInt(condition), viewCount);
            goodsIds.add(i + 1);
            System.out.println("  - 商品#" + (i + 1) + ": " + title + " (￥" + price + ", " + category + ")");
        }
        
        System.out.println("[DataSeeder] 步骤4完成，共生成 " + goodsIds.size() + " 个商品");
        return goodsIds;
    }

    // ========== 阶段5: 生成评论 ==========
    private void step5_createComments(List<String> userIds, List<Integer> goodsIds) {
        System.out.println("[DataSeeder] 步骤5: 生成评论数据...");
        
        // 按分类准备评论模板
        java.util.Map<String, List<String>> commentTemplates = new java.util.HashMap<>();
        commentTemplates.put("数码", List.of(
            "这个价格真不错，什么时候还有货呀？",
            "成色看起来很新，卖家靠谱吗？",
            "用过同款，体验确实不错，推荐！",
            "还能降点价吗？诚心想要",
            "支持当面交易吗？想看看实物",
            "刚刚问了卖家，回复很快～",
            "有保修吗？二手怕出问题",
            "出吗出吗！能便宜点不～"
        ));
        commentTemplates.put("书籍", List.of(
            "是哪一版的？需要最新版",
            "书况怎么样？有没有笔记和划线",
            "这个版本确实好，比上一版改进很多",
            "考研必备，支持一下！",
            "包邮吗？想要两本一起买",
            "有目录可以看一下吗？",
            "学长这个书确实有用，买！",
            "二手书性价比高，赞！"
        ));
        commentTemplates.put("服装", List.of(
            "M码适合165/55kg吗？",
            "什么材质的？会起球吗？",
            "买大了穿不了，二手出很划算",
            "有实物图吗？想看下细节",
            "支持退换吗？想试穿一下",
            "颜色和图片一样吗？",
            "有吊牌吗？想要全新的感觉",
            "尺码标准吗？偏大偏小？"
        ));
        commentTemplates.put("生活用品", List.of(
            "东西看着不错，质量怎么样？",
            "能送点小东西吗？",
            "同城可以送上门吗？",
            "宿舍用刚好，谢谢！",
            "这个牌子用过的，确实好用",
            "有使用痕迹吗？",
            "便宜出了？那必须来一个",
            "能砍价吗？学生党预算有限"
        ));
        commentTemplates.put("运动", List.of(
            "这个牌子的羽毛球拍怎么样？",
            "用了多久了？还新不新",
            "正好需要，支持校园运动！",
            "能送个球吗？送了立马下单",
            "校队同款，质量没话说",
            "有拍包吗？",
            "适合新手用吗？还是进阶的？",
            "能试打一下吗？"
        ));
        commentTemplates.put("美妆", List.of(
            "色号试了吗？适合黄皮吗？",
            "正品保障吗？怕买到假的",
            "这个色号超美！已经种草了",
            "有保质期吗？",
            "买过同款，确实好用",
            "能包邮吗？想多买几支",
            "有赠品吗？",
            "包装好看，送人也不错"
        ));
        commentTemplates.put("家居", List.of(
            "尺寸多大？宿舍放得下吗？",
            "结实吗？怕不好用",
            "搬家价确实划算",
            "有实物照片吗？",
            "这个大小刚好！太适合宿舍了",
            "能送货上楼吗？",
            "用着不错的东西才推荐",
            "学生党刚需，买了！"
        ));
        
        String commentSql = "INSERT INTO `comments` (`goods_id`, `user_id`, `nickname`, `content`, `create_time`) VALUES (?, ?, ?, ?, NOW())";
        
        // 商品分类映射
        String[] categoryMap = {
            "数码", "数码", "数码", "数码", "数码", "数码",
            "书籍", "书籍", "书籍", "书籍", "书籍",
            "服装", "服装", "服装",
            "生活用品", "生活用品", "生活用品",
            "运动", "运动", "运动",
            "美妆", "美妆",
            "家居", "家居", "家居"
        };
        
        int totalComments = 0;
        
        for (int i = 0; i < goodsIds.size(); i++) {
            int goodsId = goodsIds.get(i);
            String category = categoryMap[i];
            List<String> templates = commentTemplates.getOrDefault(category, commentTemplates.get("生活用品"));
            
            // 每个商品生成 6-8 条评论
            int commentCount = RAND.nextInt(3) + 6; // 6-8
            
            // 创建 username -> id 映射用于评论
            java.util.Map<String, Integer> commentUserToId = new java.util.HashMap<>();
            for (String uname : userIds) {
                Integer cid = jdbcTemplate.queryForObject(
                    "SELECT id FROM `user` WHERE username = ?", Integer.class, uname);
                commentUserToId.put(uname, cid);
            }

            // 选6-8个不同的用户来评论（不包含发布者）
            String[] usedUsers = new String[commentCount];
            java.util.Set<String> usedSet = new java.util.HashSet<>();
            int idx = 0;
            while (idx < commentCount) {
                String user = userIds.get(RAND.nextInt(userIds.size()));
                if (!usedSet.contains(user)) {
                    usedSet.add(user);
                    usedUsers[idx++] = user;
                }
            }
            
            // 随机选评论并打乱顺序
            java.util.List<String> chosenComments = new ArrayList<>();
            java.util.List<Integer> templateIndices = new ArrayList<>();
            for (int t = 0; t < templates.size(); t++) {
                templateIndices.add(t);
            }
            java.util.Collections.shuffle(templateIndices);
            for (int t = 0; t < commentCount && t < templates.size(); t++) {
                chosenComments.add(templates.get(templateIndices.get(t)));
            }
            
            // 也打乱用户
            java.util.Collections.shuffle(java.util.Arrays.asList(usedUsers));
            
            for (int c = 0; c < commentCount; c++) {
                jdbcTemplate.update(commentSql, goodsId, commentUserToId.get(usedUsers[c]), commentUserToId.get(usedUsers[c]), chosenComments.get(c));
            }
            
            totalComments += commentCount;
            System.out.println("  - 商品#" + (i + 1) + " 生成 " + commentCount + " 条评论");
        }
        
        System.out.println("[DataSeeder] 步骤5完成，共生成 " + totalComments + " 条评论");
    }
}
