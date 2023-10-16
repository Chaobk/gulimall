package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {

    public static ExecutorService service = Executors.newFixedThreadPool(10);

    ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
            200,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main---start---");
//        Thread01 thread = new Thread01();
//        thread.start();
//        Thread thread = new Thread(new Runable01());
//        thread.start();

//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
//        // 阻塞等待
//        Integer integer = futureTask.get();
//        System.out.println(integer);

        // 当前系统中池只有一两个，以上三种启动线程的方式都不用。【将所有的多线程异步任务都交给线程池执行】
        // 区别：
        //      1、2不能得到返回值。3可以获取返回值
        //      1、2、3 都不能控制资源
        //      4 可以控制资源，性能稳定
        service.execute(new Runable01());

        System.out.println("main---end");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程: " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.printf("运行结果: %d", i);
        }
    }

    public static class Runable01 implements Runnable {
        @Override
        public void run() {
            System.out.printf("当前线程： %d\n", Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.printf("运行结果: %d\n", i);
        }
    }

    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            Thread.sleep(2000);
            int i = 10 / 2;
            return i;
        }
    }
}
