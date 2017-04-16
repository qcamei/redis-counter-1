/**
 *
 */

import com.hlin.counter.redis.AbstractCounter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 视屏PV计数器
 *
 * @author hailin1.wang@downjoy.com
 * @createDate 2016年3月26日
 */
@Service
public class VideoPVCounter extends AbstractCounter {

    /**
     * 前缀
     */
    private static final String prefix = "VideoPVCounter.";

    @Resource
    private VideoService videoService;

    @Override
    public String getCounterPrefix() {
        return prefix;
    }

    @Override
    protected int getCounterExpireSecond() {
        return 60 * 20;
    }

    @Override
    protected int getDBDataExpireSecond() {
        return 60 * 20;
    }

    @Override
    protected int getBaseIncrement() {
        return 1;
    }

    /**
     * 查询db数据
     */
    @Override
    protected long getDBData(String field) {

        return videoService.getPVByVideoId(Integer.valueOf(field));
    }

    /**
     * 更新值到db
     */
    @Override
    protected boolean updateDBData(String field, long pv) {
        videoService.addVideoPVById(Integer.valueOf(field), pv);
        return true;
    }

}