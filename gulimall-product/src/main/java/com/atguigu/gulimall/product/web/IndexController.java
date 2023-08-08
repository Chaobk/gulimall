package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;


    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // TODO 1.查出所有的1级分类
        List<CategoryEntity> list = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", list);
        return "index";
    }


    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @ResponseBody
    @GetMapping("hello")
    public String hello() {
        // 1.获取到锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");

        // 2.加锁
        lock.lock();
        // 如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        // 2.如果没有指定锁的超时时间，就是用30 * 1000【LockWatchdoyTimeout看门狗的默认事件】
        //  只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】
        // 看门狗时间 / 3 进行续期，续成满时间
//        lock.lock(10, TimeUnit.SECONDS);  // 10s后自动解锁，自动解锁时间一定要大于业务的执行时间

        // 1)所得自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期会被删掉
        // 2）加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认30s以后删除


        try {
            System.out.println("加锁成功。。。。" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch(Exception e) {

        } finally {
            System.out.println("释放锁"+ Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }


    /**
     * 保证一定能读到最新数据，修改期间，写锁是一个排他锁（互斥锁）。读锁是一个共享锁
     * 写锁没释放就必须等待
     * @return
     */
    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";

        RLock rLock = lock.writeLock();
        try {
            System.out.println("写锁加锁");
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30 * 1000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("释放写锁");
            rLock.unlock();
        }

        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        // 加读锁
        RLock rLock = lock.readLock();
        rLock.lock();
        System.out.println("读锁加锁");
        String s = "";
        try {
            s = (String) redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30 *1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("释放读锁");
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.acquire(); // 获取一个信号量，获取一个值

        return "ok";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();

        return "release";
    }


    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await(); // 等待闭锁都完成

        return "放假了。。。";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();
        return id + "班的人都走了...";
    }
}
