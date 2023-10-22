package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

public class FutureTest {

    static ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
            100,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 1.thenRun获取不到上个线程的执行结果，无返回值
//        CompletableFuture<Void> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("Runnable start: " + Thread.currentThread().getId());
//            int i = 10 / 3;
//            System.out.println("Runanble end: " + i);
//            return i;
//        }, executor).thenRunAsync(() -> {
//            System.out.println("任务2启动了");
//        }, executor);

        // 2.能接收上一步的执行结果，无返回值
//        CompletableFuture.supplyAsync(() -> {
//            System.out.println("Runnable start: " + Thread.currentThread().getId());
//            int i = 10 / 3;
//            System.out.println("Runanble end: " + i);
//            return i;
//        }, executor).thenAcceptAsync(res -> {
//            System.out.println("测试的线程id: " + Thread.currentThread().getId());
//            System.out.println("上一步的的执行结果: " + res);
//        }, executor);
//
//        System.out.println("main...end...");


        // 3.thenApplyAsync 能接收上一步结果，有返回值
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("Runnable start: " + Thread.currentThread().getId());
//            int i = 10 / 3;
//            System.out.println("Runanble end: " + i);
//            return i;
//        }).thenApplyAsync((res) -> {
//            System.out.println("任务2启动了: " + res);
//            return "hello";
//        }, executor);
//        System.out.println(future.get());
//        System.out.println("main...end...");


        /**
         * 两个都完成
         */
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1Runnable start: " + Thread.currentThread().getId());
            int i = 10 / 3;
            System.out.println("任务1Runanble end: " + i);
            return i;
        }, executor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2Runnable start: " + Thread.currentThread().getId());
            int i = 10 / 3;
            System.out.println("任务2Runanble end: " + i);
            return "hello";
        }, executor);

        //         runAfterBoth 获取不到前面流程的执行结果
//        future1.runAfterBothAsync(future2, () -> {
//            System.out.println("任务3 start");
//        }, executor);

        // 可以接收到前面的结果
        //    /**
        //     * Performs this operation on the given arguments.
        //     * @param t the first input argument
        //     * @param u the second input argument
        //     */
        //    void accept(T t, U u);
//        future1.thenAcceptBothAsync(future2, (f1, f2) -> {
//            System.out.println("任务3 Runnable start: " + Thread.currentThread().getId());
//            System.out.printf("%s %s", f1, f2);
//        }, executor);

        // 可以有返回值
//        System.out.println(future1.thenCombine(future2, (f1, f2) -> {
//            return f1 + ": " + f2 + " -> hello";
//        }).get());
    }

    static class RunableTest1 implements Runnable {

        @Override
        public void run() {
            System.out.println("Runable start:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("Runable end: " + i);
        }
    }
}
