# redis-counter
基于redis的实时计数器，可用于点赞等单个统计场景


# 实现逻辑
    1.将数据分为两部分，数据库取出的DBData、redis中的kv计数counter
    2.增加计数时，incr操作redis中的计数
    3.取回计数时，取DBData（同时缓存） + counter
    4.更新counter到db时，将counter利用getset置为0，然后清空DBData缓存（这个地方会有短暂的数据不一致，但是影响不大，可以通过其他方式处理，比如给一个中间状态的key取一个固定的值，更新完成后删除这个key）
    5.更新方式有定时扫描更新、设定超过伐值更新2种
