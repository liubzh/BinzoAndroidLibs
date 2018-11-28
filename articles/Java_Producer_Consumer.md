# The Evolution of the Producer-Consumer Problem in Java
**Java中关于生产者-消费者问题的演化**

*Binzo 译于2018-11-22，原文作者：Ioan Tinca*

原文链接：
https://dzone.com/articles/the-evolution-of-producer-consumer-problem-in-java

The producer-consumer problem is a classic example of a multi-process synchronization problem.<br>
生产者-消费者问题是多进程同步问题的典型例子。

For most of us, this problem is maybe the first synchronization problem that we studied back in school and were facing parallel algorithms for the first time.<br>
对于我们大多数人来说，这个问题可能是我们在学校学习并行算法时面对的第一个同步问题。

Simple as it is, it resumes the biggest challenge in parallel computing — the sharing of a single resource by multiple processes.<br>
虽然它很简单，但依然是并行计算中的最大挑战 - 通过多个进程共享单个资源。

## Problem Statement
问题描述

There are two processes, a producer and a consumer, that share a common buffer with a limited size.<br>
有两个进程，一个生产者和一个使用者，共享一个有限大小的公共缓冲区。

The producer “produces” data and stores it in the buffer, and the consumer “consumes” the data, removing it from the buffer.<br>
生产者“生产”数据并将其存储在缓冲区中，消费者“消费”数据，将其从缓冲区中删除。

Having two processes that run in parallel, we need to make sure that the producer will not put new data in the buffer when the buffer is full and the consumer won’t try to remove data from the buffer if the buffer is empty.<br>
有两个并行运行的进程，我们需要确保生成器在缓冲区已满时不会将新数据放入缓冲区，如果缓冲区为空，消费者不会从缓冲区中删除数据。

## Solution
解决方法

For solving this concurrency problem, the producer and the consumer will have to communicate with each other.<br>
为了解决这个并发问题，生产者和消费者必须相互通信。

If the buffer is full, the producer will go to sleep and will wait to be notified.<br>
如果缓冲区已满，则生产者将进入休眠状态并等待通知。

After the consumer will remove some data from the buffer, it will notify the producer, and then, the producer will start refilling the buffer again.<br>
消费者从缓冲区中删除一些数据后，它将通知生产者，然后，生产者将再次开始重新填充缓冲区。

The same process will happen if the buffer is empty, but in this case, the consumer will wait to be notified by the producer.<br>
如果缓冲区为空，则会发生相同的过程，但在这种情况下，消费者将等待生产者通知。

If this communication is not done properly, it can lead to a deadlock where both processes will wait for each other.<br>
如果通信未正确完成，则可能导致死锁，其中两个进程将彼此等待。

## Classic Approach
古典方法

Let’s see a typical Java solution to this problem.<br>
让我们看看关于这个问题的典型Java解决方案。

```java
package ProducerConsumer;

import java.util.LinkedList;
import java.util.Queue;

public class ClassicProducerConsumerExample {

    public static void main(String[] args) throws InterruptedException {
        Buffer buffer = new Buffer(2);

        Thread producerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    buffer.produce();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread consumerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    buffer.consume();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();
    }

    static class Buffer {

        private Queue<Integer> list;
        private int size;

        public Buffer(int size) {
            this.list = new LinkedList<>();
            this.size = size;
        }

        public void produce() throws InterruptedException {
            int value = 0;
            while (true) {
                synchronized (this) {
                    while (list.size() >= size) {
                        // wait for the consumer
                        wait();
                    }

                    list.add(value);

                    System.out.println("Produced " + value);

                    value++;

                    // notify the consumer
                    notify();

                    Thread.sleep(1000);
                }
            }
        }

        public void consume() throws InterruptedException {
            while (true) {
                synchronized (this) {
                    while (list.size() == 0) {
                        // wait for the producer
                        wait();
                    }

                    int value = list.poll();

                    System.out.println("Consume " + value);

                    // notify the producer
                    notify();

                    Thread.sleep(1000);
                }
            }
        }
    }
}
```

Here we have two threads, a producer and a consumer thread, which share a common buffer.<br>
我们这里有两个线程，一个生产者线程和一个消费者线程，它们共享一个公共缓冲区。

The producer thread starts producing new elements and stores them in the buffer.<br>
生产者线程开始生成新元素并将它们存储在缓冲区中。

If the buffer is full, it goes to sleep and will wait to be notified.<br>
如果缓冲区已满，它将进入休眠状态并等待通知。

Otherwise, it will put a new element in the buffer and notify the consumer.<br>
否则，它会在缓冲区中放入一个新元素并通知消费者。

Like I said before, the same process applies to the consumer. If the buffer is empty, the consumer will wait to be notified by the producer.<br>
就像我之前说的那样，消费者也是同样的过程。如果缓冲区为空，则消费者将等待生产者通知。

Otherwise, it will remove an element from the ```buffer``` and it will notify the consumer.<br>
否则，它从缓冲区中删除一个元素，然后通知消费者。

As you can see, in the previous example, both jobs are managed by the ```buffer``` object.<br>
在前面的示例中可以看到，两个作业都由```buffer```对象管理。

The threads are just calling ```buffer.produce()``` and ```buffer.consume()```, and everything is done by these two methods.<br>
线程只是调用```buffer.produce()```和```buffer.consume()```，一切都是通过这两个方法完成的。

This is a debatable subject, but in my opinion, the buffer shouldn’t be responsible for creating or removing the elements.<br>
这个问题有待商榷，在我看来，缓冲区不应该负责创建或删除元素。

Of course, that depends on what you want to achieve, but in this case, the buffer should be responsible just for storing and pooling elements in a thread-safe manner, not for producing the elements.<br>
当然，这取决于你的实现目的，但在这种情况下，缓冲区应该负责以线程安全的方式存储和汇集元素，而不是生成元素。

So, let’s move the produce and consume logic out of the ```buffer``` object.<br>
所以，让我们把生产和消费逻辑从```buffer```对象中移出来。

```java
package ProducerConsumer;

import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumerExample2 {

    public static void main(String[] args) throws InterruptedException {
        Buffer buffer = new Buffer(2);

        Thread producerThread = new Thread(() -> {
            try {
                int value = 0;
                while (true) {
                    buffer.add(value);

                    System.out.println("Produced " + value);

                    value ++;

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread consumerThread = new Thread(() -> {
            try {
                while (true) {
                    int value = buffer.poll();

                    System.out.println("Consume " + value);

                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();
    }

    static class Buffer {

        private Queue<Integer> list;
        private int size;

        public Buffer(int size) {
            this.list = new LinkedList<>();
            this.size = size;
        }

        public void add(int value) throws InterruptedException {
            synchronized (this) {
                while (list.size() >= size) {
                    wait();
                }
                list.add(value);
                notify();
            }
        }

        public int poll() throws InterruptedException {
            synchronized (this) {
                while (list.size() == 0) {
                    wait();
                }

                int value = list.poll();
                notify();
                return value;
            }
        }
    }
}
```

That’s better. Now, the buffer is responsible for storing and removing the elements in a thread-safe manner.<br>
这样好多了。现在，缓冲区仅负责以线程安全的方式存储和移除元素。

## Blocking Queue
阻塞队列

However, we can further improve this.<br>
但是，我们还可以做进一步改进。

In the previous example, we’ve created a buffer that, when storing an element, waits for a slot to become available in case there is no more space, and, on polling, in case that the buffer is empty, it waits for an element to become available, making the storing and removing operations thread-safe.<br>
在前面的示例中，我们创建了一个缓冲区，在存储元素时，在没有更多空间的情况下等待一个可用空间，以及，缓冲区为空时，等待一个有效元素进来，使存储和删除操作成为线程安全的。

But, Java already has a collection for this.<br>
但是，Java已经有了这个集合。

It’s called a BlockingQueue and, as it is described here, this is a queue that is thread-safe to put into and take instances from. It does exactly what we want. So, if we use a BlockingQueue in our example, we don’t have to implement the waiting and notifying mechanism.<br>
它被称为```BlockingQueue```，正如这里所描述的，这是一个可以放入线程并从中获取实例的队列。它完全符合我们的要求。因此，如果我们在示例中使用```BlockingQueue```，则不必实现等待和通知机制。

Let’s see how it looks.<br>
让我们看看它是啥样的。

```java
package ProducerConsumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ProducerConsumerWithBlockingQueue {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> blockingQueue = new LinkedBlockingDeque<>(2);

        Thread producerThread = new Thread(() -> {
            try {
                int value = 0;
                while (true) {
                    blockingQueue.put(value);

                    System.out.println("Produced " + value);

                    value++;

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread consumerThread = new Thread(() -> {
            try {
                while (true) {
                    int value = blockingQueue.take();

                    System.out.println("Consume " + value);

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();
    }
}
```

The runnables look exactly as they did before.<br>
这看起来与之前一样。

They produce and consume elements in the same way.<br>
它们以相同的方式生产和消费元素。

The only difference is that here we use a blockingQueue instead of our buffer object.<br>
唯一的区别是这里我们使用的是```BlockingQueue```而不是我们的```buffer```对象。

## Some Details About the Blocking Queue
关于阻塞队列的一些详细信息

There are two types of BlockingQueue:<br>
```BlockingQueue```有两种类型：
* Unbounded queue<br>
无界队列
* Bounded queue<br>
有界队列

An unbounded queue can grow almost indefinitely, and the add operations are not blocking.<br>
无界队列几乎可以无限增长，并且添加操作不会阻塞。
You can create an unbounded queue like this:<br>
你可以像这样创建一个无界队列：

```java
BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();
```

In this case, since the add operations are not blocking, the producer doesn’t have to wait when adding new elements.<br>
在这种情况下，由于添加操作没有阻塞，因此生产者不必在添加新元素时等待。

Every time when the producer wants to add a new element, the queue will store it. But, there is catch here.<br>
每次生产者想要添加新元素时，队列都会存储它。但是，这里有一个问题。

If the consumer doesn’t remove elements faster than the producer is adding new elements, then the memory will fill up and we will get an OutOfMemory exception.<br>
如果消费者没有比生产者添加新元素更快地删除元素，那么内存将填满，将会引发```OutOfMemory```异常。

The bounded queue, instead, has a fixed size. You can create one like this:<br>
相反，有界队列具有固定大小。你可以像这样创建一个：

```java
BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>(10);
```

The main difference is that using a bounded queue, if the queue is full and the producer tries to store another element, depending on what method is used for adding, the queue will block until it will be enough space.<br>
使用有界队列的主要区别在于，如果队列已满并且生产者尝试存储另一个元素，则根据用于添加的方法，队列将阻塞，直到它有足够的空间。

There are four methods for adding elements in a blocking queue:<br>
在阻塞队列中添加元素有四种方法：

* add() – returns true if the insertion was successful, otherwise, it will throw an ```IllegalStateException```<br>
add() - 如果插入成功则返回true，否则将抛出```IllegalStateException```
* put() – inserts an element into the queue and waits for a free slot if necessary<br>
put() - 在队列中插入一个元素，并在必要时等待一个空闲位置
* offer() – returns true if the insertion was successful, otherwise, it returns false<br>
offer() - 如果插入成功则返回true，否则返回false
* offer(E e, long timeout, TimeUnit unit) – inserts an element into the queue if it is not full, or waits for an available slot within a specified timeout<br>
offer(E e，long timeout，TimeUnit unit) - 如果队列未满，则将元素插入队列，或等待指定超时内的可用位置

So, if you use the put() method and the queue is full, the producer will have to wait until there is a free slot.<br>
因此，如果您使用```put()```方法并且队列已满，则生产者必须等到有空闲位置时再进行生产操作。

That’s what we used in the previous example, and this will work in the same way as ProducerConsumerExample2.<br>
这就是我们在前面的示例中使用的内容，这与ProducerConsumerExample2的工作方式相同。

## Using a Thread Pool
使用一个线程池

What else can we improve here?<br>
我们还能在这方面做进一步改进吗？

Let’s analyze what we did.<br>
让我们来分析一下我们都做了什么。

We’ve instantiated two threads, one that puts some elements in the blocking queue, the producer, and another that retrieves elements from the queue, the consumer.<br>
我们实例化了两个线程，一个将一些元素放入阻塞队列（生产者），另一个从队列中回收元素（消费者）。

But, good software techniques suggest that creating and destroying threads manually is bad practice.<br>
但是，大牛建议：手动创建和销毁线程是不好的做法。
Thread creation is an expensive task. Each thread creation implies the following steps:<br>
线程创建是一项昂贵的任务。每个线程创建意味着以下步骤：

* It allocates memory for a thread stack
它为线程堆栈分配内存
* The OS creates a native thread corresponding to the Java thread
操作系统创建与Java线程对应的本地线程
* Descriptors relating to the thread are added to the JVM internal data structures
与线程相关的描述符被添加到JVM内部数据结构中

Don’t get me wrong.<br>
别误会我的意思。

There is nothing wrong with having more threads.<br>
拥有更多线程没有任何问题。

That’s how parallelism works. The problem here is that we’ve created them “manually”.<br>
这就是并行性的工作原理。这里的问题是我们“手动”创建了它们。

That’s the bad practice.<br>
这是不好的做法。

If we create them manually, besides the creation’s cost, another problem is that we don’t have control over how many of them are running at the same time.<br>
如果我们手动创建它们，除了创建成本之外，另一个问题是我们无法控制它们中有多少同时运行。

For example, if millions of requests are reaching a server app, and for each request, a new thread is created, then millions of threads will run in parallel and this could lead to a [thread starvation](https://en.wikipedia.org/wiki/Starvation_(computer_science)).<br>
例如，如果数百万个请求到达服务器应用程序，并且对于每个请求，则创建新线程，然后数百万个线程将并行运行，这可能导致[线程饥饿](https://en.wikipedia.org/wiki/Starvation_(computer_science))。

So, we need a way to strategically manage threads. And here comes the thread pools.<br>
因此，我们需要一种战略性管理线程的方法。这里是线程池。

Thread pools handle the threads' life cycle based on a selected strategy.<br>
线程池根据选定的策略处理线程的生命周期。

It holds a limited number of idle threads and reuses them when it needs to solve tasks.<br>
它拥有有限数量的空闲线程，并在需要解决任务时重用它们。

This way, we don’t have to create a new thread every time for a new request, and therefore, we can avoid reaching a thread starvation,<br>
这样，我们不必每次都为新请求创建一个新线程，因此，我们可以避免线程饥饿，

The Java thread pool implementation consists of:<br>
Java线程池实现包括：

* A task queue
一个任务队列
* A collection of worker threads
一个工作线程的集合
* A thread factory
一个线程工厂
* Metadata for managing thread pool state.
用于管理线程池状态的元数据。

For running some tasks concurrently, you have to put them in the task queue.<br>
要同时运行一些任务，必须将它们放在任务队列中。

Then, when a thread is available, it will receive a task and run it.<br>
然后，当一个线程可用时，它将接收一个任务并运行它。

The more available threads, the more tasks that are executed in parallel.<br>
可用线程越多，并行执行的任务就越多。

Beside the thread lifecycle management, another advantage when working with a thread pool is that when you plan on how to split the work to be executed concurrently, you can think in a more functional way.<br>
除了线程生命周期管理之外，使用线程池时的另一个优点是，当你计划如何拆分要同时执行的工作时，可以以更实用的方式进行思考。

The unit of parallelism is not the thread anymore; it’s the task.<br>
并行的单位不再是线程;而是任务。

You design some tasks that are executed concurrently, and not some threads that share a common memory and are running in parallel.<br>
你设计了一些并发执行的任务，而不是一些共享公共内存并且并行运行的线程。

Thinking in a functional way can help us avoid some common multithreading problems, like deadlocks or data races.<br>
以功能方式思考可以帮助我们避免一些常见的多线程问题，例如死锁或数据争用。

Nothing can stop us from reaching into these problems again, but, because using the functional paradigm, we don’t imperatively synchronize the concurrent computation (with locks).<br>
没有什么能阻止我们再次陷入这些问题，但是，因为使用功能范例，我们不会强制性地同步并发计算（使用锁）。

This is far less happening than working directly with threads and shared memory.<br>
这比直接使用线程和共享内存要少得多。

This is not the case in our example since the tasks share a blocking queue, but I just wanted to highlight this advantage.<br>
在我们的示例中不是这种情况，因为任务共享一个阻塞队列，但我只想强调这一优势。

[Here](https://allegro.tech/2015/05/thread-pools.html) and [here](https://dzone.com/articles/getting-the-most-out-of-the-java-thread-pool) you can find more details about thread pools.
在[这里]((https://allegro.tech/2015/05/thread-pools.html))和[这里](https://dzone.com/articles/getting-the-most-out-of-the-java-thread-pool)你可以找到有关线程池的更多详细信息。

With all of this being said, let’s see how our example looks using a thread pool.<br>
有了这些，让我们看看我们的示例如何使用线程池。

```java
package ProducerConsumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class ProducerConsumerExecutorService {

    public static void main(String[] args) {
        BlockingQueue<Integer> blockingQueue = new LinkedBlockingDeque<>(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable producerTask = () -> {
            try {
                int value = 0;
                while (true) {
                    blockingQueue.put(value);

                    System.out.println("Produced " + value);

                    value++;

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Runnable consumerTask = () -> {
            try {
                while (true) {
                    int value = blockingQueue.take();

                    System.out.println("Consume " + value);

                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        executor.execute(producerTask);
        executor.execute(consumerTask);

        executor.shutdown();
    }
}
```

The difference here is that, instead of manually creating and running the consumer and producer threads, we build a thread pool, and it will receive two tasks, a producer and a consumer task.<br>
这里的不同之处在于，我们不是手动创建和运行消费者和生产者线程，而是构建一个线程池，它将接收两个任务，即生产者和消费者任务。

The producer and consumer tasks are actually the same runnables that were used in the previous example.<br>
生产者和消费者任务实际上与前一个示例中使用的runnable相同。

Now, the executor (the thread pool implementation) receives the tasks, and its working threads will execute them.<br>
现在，执行程序（线程池实现）接收任务，其工作线程将执行它们。

In our simple case, everything will work the same as before.<br>
在我们简单的例子中，一切都将像以前一样工作。

Just like in previous examples, we still have two threads, and they still produce and consume elements in the same way.<br>
就像前面的例子一样，我们仍然有两个线程，它们仍然以相同的方式生产和消费元素。

So, we don’t have a performance improvement here, but the code looks cleaner.<br>
因此，我们在这里没有性能改进，但代码看起来更干净。

We no longer build the threads manually, but, instead, we just specify what we want.<br>
我们不再手动构建线程，而是指定我们想要的内容。

And, we want a way to execute some tasks in parallel.<br>
并且，我们想要一种并行执行某些任务的方法。

So, when you use a thread pool, you don’t have to think to threads as the unit of parallelism, but instead, you think to some tasks that are executed concurrently.<br>
因此，当你使用线程池时，不必将线程视为并行的单位，而是考虑一些并发执行的任务。

That’s what you need to know, and the executor will handle the rest.<br>
这就是你需要知道的，执行者将处理剩下的事情。

It will receive some tasks, and then, it will execute them using the available working threads.<br>
它将接收一些任务，然后在可用的工作线程中执行这些任务。

## Summary
总结

First, we saw the “traditional” solution of a consumer-producer problem.<br>
首先，我们看到了消费者-生产者问题的“传统”解决方案。

We try to not reinvent the wheel when is not necessary, but instead, we want to reuse already tested solutions.<br>
我们试图在没有必要的情况下不重复造轮子，而是重新使用已经测试过的解决方案。

So, instead of writing down a wait/notify system, why not use the Java blocking queue that already offers that?<br>
那么，与其说自己写等待/通知系统，倒不如使用已经提供的Java阻塞队列。

And also, we can get rid of manually creating threads when Java provides us with a thread pool that manages thread lifecycle very efficiently already.<br>
而且，当Java为我们提供一个非常有效地管理线程生命周期的线程池时，我们再也不用手动创建线程了。

With these improvements, the solutions of the consumer-producer problem look more reliable and understandable.<br>
通过这些改进，消费者-生产者问题的解决方案看起来更可靠、更易懂。