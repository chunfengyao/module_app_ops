package com.yaocf.support.ops.natives

/**
 * Linux中所有信号和值，以及对应的含义
#define SIGHUP      1   /* Hangup (POSIX).  */
#define SIGINT      2   /* Interrupt (ANSI).  */
#define SIGQUIT     3   /* Quit (POSIX).  */
#define SIGILL      4   /* Illegal instruction (ANSI).  */
#define SIGTRAP     5   /* Trace trap (POSIX).  */
#define SIGABRT     6   /* Abort (ANSI).  */
#define SIGIOT      6   /* IOT trap (4.2 BSD).  */
#define SIGBUS      7   /* BUS error (4.2 BSD).  */
#define SIGFPE      8   /* Floating-point exception (ANSI).  */
#define SIGKILL     9   /* Kill, unblockable (POSIX).  */
#define SIGUSR1     10  /* User-defined signal 1 (POSIX).  */
#define SIGSEGV     11  /* Segmentation violation (ANSI).  */
#define SIGUSR2     12  /* User-defined signal 2 (POSIX).  */
#define SIGPIPE     13  /* Broken pipe (POSIX).  */
#define SIGALRM     14  /* Alarm clock (POSIX).  */
#define SIGTERM     15  /* Termination (ANSI).  */
#define SIGSTKFLT   16  /* Stack fault.  */
#define SIGCLD      SIGCHLD /* Same as SIGCHLD (System V).  */
#define SIGCHLD     17  /* Child status has changed (POSIX).  */
#define SIGCONT     18  /* Continue (POSIX).  */
#define SIGSTOP     19  /* Stop, unblockable (POSIX).  */
#define SIGTSTP     20  /* Keyboard stop (POSIX).  */
#define SIGTTIN     21  /* Background read from tty (POSIX).  */
#define SIGTTOU     22  /* Background write to tty (POSIX).  */
#define SIGURG      23  /* Urgent condition on socket (4.2 BSD).  */
#define SIGXCPU     24  /* CPU limit exceeded (4.2 BSD).  */
#define SIGXFSZ     25  /* File size limit exceeded (4.2 BSD).  */
#define SIGVTALRM   26  /* Virtual alarm clock (4.2 BSD).  */
#define SIGPROF     27  /* Profiling alarm clock (4.2 BSD).  */
#define SIGWINCH    28  /* Window size change (4.3 BSD, Sun).  */
#define SIGPOLL     SIGIO   /* Pollable event occurred (System V).  */
#define SIGIO       29  /* I/O now possible (4.2 BSD).  */
#define SIGPWR      30  /* Power failure restart (System V).  */
#define SIGSYS      31  /* Bad system call.  */
#define SIGUNUSED   31
 */

/**
 * 一般只要处理这几个！
信号	    信号值	含义	                    备注	                                            在Android中默认行为
SIGSEGV	11	    访问无效地址	            如试图访问未分配给自己的内存	                    生成tombstone文件,然后退出
SIGBUS	7	    非法地址	                包括内存地址对齐(alignment)出错。	                生成tombstone文件,然后退出
SIGABRT	6	    调用abort函数生成的信号。	                                                生成tombstone文件,然后退出
SIGFPE	8	    浮点计算错误。	        包括浮点运算错误, 还包括溢出及除数为0等算数运算错误	生成tombstone文件,然后退出
SIGILL	4	    非法指令错误。	        非法指令错误。	                                生成tombstone文件,然后退出
SIGTRAP	5	    硬件错误（通常为断点指令）	                                                生成tombstone文件,然后退出
 */

/**
 * created by:   yaochunfeng
 * on:           2020/3/31 14:25
 * Des:          Used to 信号量
 * Email:        yaochunfeng@wondersgroup.com
 *
 */
class SysSignalConst {
    companion object {
        const val SIGHUP = 1
        const val SIGINT = 2
        const val SIGQUIT = 3
        const val SIGILL = 4
        const val SIGTRAP = 5
        const val SIGABRT = 6
        const val SIGBUS = 7
        const val SIGFPE = 8
        const val SIGSEGV = 11
    }
}