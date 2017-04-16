import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 类注释/描述
 *
 * @author hailin0@yeah.net
 * @date 2017-4-16 22:46
 */
@Component
public class VideoService {

    private AtomicLong pvDBCounter = new AtomicLong(1);

    public long getPVByVideoId(Integer id) {
        return pvDBCounter.get();
    }

    public void addVideoPVById(Integer id, long pv) {
        pvDBCounter.addAndGet(pv);
    }
}
