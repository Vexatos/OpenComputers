package li.cil.oc.util

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import li.cil.oc.OpenComputers
import li.cil.oc.Settings

import scala.collection.mutable

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

  val handlers = mutable.Set.empty[WrappedThreadPool]

  def createWrappedPool(name: String, threads: Int): WrappedThreadPool = {
    val handler = new WrappedThreadPool(name, threads)
    handlers += handler
    handler
  }
}

class WrappedThreadPool(val name: String, val threads: Int) {
  private var _threadPool: ScheduledExecutorService = _

  def withPool(f: ScheduledExecutorService => Future[_], requiresPool: Boolean = true): Option[Future[_]] = {
    if (_threadPool == null) {
      OpenComputers.log.warn("Error handling file saving: Did the server never start?")
      if (requiresPool) {
        OpenComputers.log.warn("Creating new thread pool.")
        newThreadPool()
      } else {
        return None
      }
    } else if (_threadPool.isShutdown || _threadPool.isTerminated) {
      OpenComputers.log.warn("Error handling file saving: Thread pool shut down!")
      if (requiresPool) {
        OpenComputers.log.warn("Creating new thread pool.")
        newThreadPool()
      } else {
        return None
      }
    }
    Option(f(_threadPool))
  }

  def newThreadPool(): Unit = {
    if (_threadPool != null && !_threadPool.isTerminated) {
      _threadPool.shutdownNow()
    }
    _threadPool = ThreadPoolFactory.create(name, threads)
  }

  def waitForSaving(): Unit = withPool(threadPool => {
    try {
      threadPool.shutdown()
      var terminated = threadPool.awaitTermination(15, TimeUnit.SECONDS)
      if (!terminated) {
        OpenComputers.log.warn("Warning: Saving the filesystem has already taken 15 seconds!")
        terminated = threadPool.awaitTermination(105, TimeUnit.SECONDS)
        if (!terminated) {
          OpenComputers.log.error("Warning: Saving the filesystem has already taken two minutes! Aborting")
          threadPool.shutdownNow()
        }
      }
    } catch {
      case e: InterruptedException => e.printStackTrace()
    }
    null
  }, requiresPool = false)
}
