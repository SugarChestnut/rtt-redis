//package cn.rentaotao.redis.transational;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.Transaction;
//
//import java.util.List;
//
///**
// * @author rtt
// * @date 2024/7/22 13:36
// */
//public class WatchDogExample {
//
//    /*
//        while True:
//            do_watch();
//            commands();
//            multi();
//            send_commands();
//            try:
//                exec();
//                break;
//            except WatchError:
//                continue;
//
//        无法完全保证事务的隔离性
//     */
//
//    public static void main(String[] args) {
//        Jedis jedis = new Jedis("127.0.0.1", 6973);
//        String key = "user";
//        for (;;) {
//            // 监视key，如果key被修改事务会执行失败，回滚
//            jedis.watch(key);
//            // 开启事务，获取事务对象
//            Transaction transaction = jedis.multi();
//            // 使用事务执行命令
//            transaction.set(key, "jxz");
//            List<Object> result = transaction.exec();
//            if (result != null) {
//                break;
//            }
//        }
//    }
//}
