package li.cil.oc.util

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import li.cil.oc.Settings

object ThreadPoolFactory {
  val priority = {
    val custom = Settings.get.threadPriority
    if (custom < 1) Thread.MIN_PRIORITY + (Thread.NORM_PRIORITY - Thread.MIN_PRIORITY) / 2
    else custom max Thread.MIN_PRIORITY min Thread.MAX_PRIORITY
  }

  class OCThreadFactory(name: String) extends ThreadFactory {
    private val baseName = "OpenComputers-" + name + "-"

    private val threadNumber = new AtomicInteger(1)

    private val group = System.getSecurityManager match {
      case null => Thread.currentThread().getThreadGroup
      case s => s.getThreadGroup
    }

    def newThread(r: Runnable) = {
      val thread = new Thread(group, r, baseName + threadNumber.getAndIncrement)
      if (!thread.isDaemon) {
        thread.setDaemon(true)
      }
      if (thread.getPriority != priority) {
        thread.setPriority(priority)
      }
      thread
    }
  }

  def create(name: String, threads: Int): ScheduledExecutorService = Executors.newScheduledThreadPool(threads, new OCThreadFactory(name))

  def createWithQueue(name: String, threads: Int, queue: BlockingQueue[Runnable]): ThreadPoolExecutor =
    new ThreadPoolExecutor(threads, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, queue, new OCThreadFactory(name))
}
