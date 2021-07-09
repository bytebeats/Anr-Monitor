# Anr-Monitor
Android Performance Tools. ANR, Dead locks and lags.

<br>Android性能监控工具. 包括了对 ANR 信息的收集, 死锁时线程信息的收集以及卡顿的监测.

## ANR信息收集原理
<br>通过在后台开启一条线程, 只要没有被打断, 就一直在循环监测.
<br>每一次循环开始的时候, 向Handler(Looper.getMainLooper())发送一条消息, 用于取消收集 ANR 的开始的标签.
<br>然后子线程休息指定时间(可以自由设置).
<br>如果此时主线程卡顿了指定时间, 那么子线程从休眠中唤醒, 此时收集 ANR 的标签没有取消, 则开始收集线程(全部线程或者主线程或者特定类型的线程)的堆栈信息, 通过回调接收.

## 卡顿的监测原理
<br>通过 Choreographer#postFrameCallback 或者 Looper.gerMainLooper().setPrinter 方式,
<br>传入自己的 FrameCallback 或者 Printer.
<br>通过对doFrame(frameTimeNanos)/println(message) 计算每一次 doFrame/println 的时间间隔,
<br>计算丢帧或者 Looper 中每一个 Message 执行的耗时
<br>从而判断应用的卡顿情况.

## 死锁时线程信息的收集原理
<br>主要是在发生死锁时, 通过 Android 系统底层函数 Monitor#GetLockOwnerThreadId 或者锁此时的持有者线程(通过反射该线程的 nativePeer 从当前线程查找)
<br>通过 Thread#enumerate(Thread[])获取全部线程, 并利用算法查找此时竞争同一把锁的线程竞争闭环, 从而找出死锁的信息.
<br>因为需要 Hook 底层 Monitor#GetLockOwnerThreadId 函数, 所以使用了 JNI 和 NDK 技术.

## How to use?
<br>使用指定地址的 MavenCentral:
<br>在根build.gradle文件中, 添加:
`    repositories {
           maven { url('https://repo1.maven.org/maven2/') }
           ...
       }`
<br>在 module build.gradle 文件中, 添加:
`    //load from maven central
       implementation('io.github.bytebeats:anr:1.0.0')
       implementation('io.github.bytebeats:lags:1.0.1')
       implementation('io.github.bytebeats:deadlock:1.0.0')`
<br>在自定义的 Application 中:
`class APMApplication : Application() {
       val anrMonitor = AnrMonitor(3000)
       val anrMonitor = AnrMonitor2(3000)

       val silentAnrListener = object : AnrListener {
           override fun onAppNotResponding(error: AnrError) {
               Log.d("anr-log", "onAppNotResponding", error)
           }
       }

       var duration = 4L

       override fun onCreate() {
           super.onCreate()
           AnrLog.logStackTrace = false
           anrMonitor.setIgnoreDebugger(true)
               .setReportAllThreads()
               .setAnrListener(object : AnrListener {
                   override fun onAppNotResponding(error: AnrError) {
                       AnrLog.logd("onAppNotResponding")
                       AnrLog.logd(error)
                       try {
                           ObjectOutputStream(ByteArrayOutputStream()).writeObject(error)
                       } catch (e: IOException) {
                           throw RuntimeException(e)
                       }
                       AnrLog.logd("Anr Error was successfully serialized")
                       throw error
                   }
               }).setAnrInterceptor(object : AnrInterceptor {
                   override fun intercept(duration: Long): Long {
                       val ret = this@APMApplication.duration - duration
                       if (ret > 0) {
                           AnrLog.logd(
                               "Intercepted ANR that is too short ($duration ms), postponing for $ret ms."
                           )
                       }
                       return ret
                   }
               })
               .setOnInterruptedListener(object : OnInterruptedListener {
                   override fun onInterrupted(e: InterruptedException) {
                       throw e
                   }
               })
           ProcessLifecycleOwner.get().lifecycle.addObserver(anrMonitor)

           val lagMonitor = LagMonitor.Builder(this.applicationContext)
               .setThresholdTimeMillis(3000L)
               .setLagLogEnabled(true)
               .setMonitorMode(LagMonitor.MonitorMode.UI)
               .setOnFrameJankListener(object : OnFrameJankListener {
                   override fun onJank(janks: Int) {
                       Log.d("lag-log", "janks: $janks")
                   }
               })
               .setOnUIThreadRunListener(object : OnUIThreadBlockListener {
                   override fun onBlock(lagTime: Long, uiRunTime: Long) {
                       Log.d("lag-log", "lagTime: $lagTime,  uiRunTime: $uiRunTime")
                   }
               })
               .setOnProcessNotRespondingListener(object : OnProcessNotRespondingListener {
                   override fun onNotResponding(processInfo: String?) {
                       Log.d("lag-log", "processInfo: $processInfo")
                   }
               })
               .build()
           ProcessLifecycleOwner.get().lifecycle.addObserver(lagMonitor)
       }

       override fun onTerminate() {
           super.onTerminate()
           anrMonitor.onAppTerminate()
       }
   }`

## Stargazers over time

[![Stargazers over time](https://starchart.cc/bytebeats/Anr-Monitor.svg)](https://starchart.cc/bytebeats/Anr-Monitor)
