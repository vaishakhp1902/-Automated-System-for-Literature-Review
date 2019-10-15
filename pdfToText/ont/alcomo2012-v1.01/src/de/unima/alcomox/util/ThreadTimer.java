// *****************************************************************************
//
// Copyright (c) 2011 Christian Meilicke (University of Mannheim)
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software,
// and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
// IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************************************

package de.unima.alcomox.util;



import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Dominique Ritze
 */
public class ThreadTimer {

    private Thread thread;

    //time after thread is killed when no result delivered
    private long intervalInMilliSeconds;
    //used to stop the application while executing the thread
    private boolean concurrentExecution = true;

    /**
     * Constructor for a thread.
     *
     * @param thread
     * @param intervalInMilliSeconds
     */
    public ThreadTimer(Thread thread, long intervalInMilliSeconds) {
        this.thread = thread;
        this.intervalInMilliSeconds = intervalInMilliSeconds;
    }

    /**
     * Method to start the timer, which will kill the thread
     * after a specified period of time if no result deliviered.
     *
     */
    public void start() {
        if (thread == null) {
            throw new IllegalStateException("Thread must not be null !");
        }

        //create new Timer as daemon thread and schedule the corresponding task
        //the task will be executed after iintervalInMilliSeconds seconds
        new Timer(true).schedule(new ThreadInterruptTask(thread), intervalInMilliSeconds);
        //start the thread
        thread.start();

        //if no concurrent execution is required, wait for the thread
        //until app is executed any further
        if (!concurrentExecution) {
            try {
                thread.join();
            } catch (InterruptedException ie) {
                return;
            }
        }

    }

    /**
     * Disable the concurrent execution of the "own" thread and 
     * the regular application thread.
     *
     * @param concurrentExecution
     */
    public void setConcurrentExecution(boolean concurrentExecution) {
        this.concurrentExecution = concurrentExecution;
    }
}


/**
 *
 * @author Dominique Ritze
 */
class ThreadInterruptTask extends TimerTask {

    private Thread thread;

    /**
     *
     * @param thread
     */
    public ThreadInterruptTask(Thread thread) {
        this.thread = thread;
    }

    @Override
    public void run() {
        //just interrupt the thread
        thread.interrupt();
    }

}
