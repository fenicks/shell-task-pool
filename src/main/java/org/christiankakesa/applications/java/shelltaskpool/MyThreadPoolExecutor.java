package org.christiankakesa.applications.java.shelltaskpool;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Specific ThreadPoolExecutor.
 */
class MyThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger LOG = Logger.getLogger(MyThreadPoolExecutor.class);

    public MyThreadPoolExecutor(int poolSize, int maxPoolSize) {
        super(poolSize, maxPoolSize, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE));
        this.myInit();
    }
    
    /**
     * Global initialization.
     */
    private void myInit() {
        Batch.getInstance().getBatchStatus().setStatus(Batch.Status.STARTED);
        Batch.getInstance().setStartDate(Calendar.getInstance().getTime());
        // We need synchronized here because "+" operator is not thread safe
        synchronized (MyThreadPoolExecutor.class) {
            Logger.getLogger("STDOUT").log(Level.INFO, "batch:start|id:" + Batch.getInstance().getId()
                    + "|name:" + Batch.getInstance().getName()
                    + "|parameters:" + Batch.getInstance().getStringParameters()
                    + "|workers:" + Batch.getInstance().getNumberOfWorkers()
                    + "|number_of_jobs:" + Batch.JOBS_STORE.size()
                    + "|jobs_file:" + Batch.getInstance().getJobsFile()
                    + "|log_dir:" + Batch.getInstance().getLogDirectory()
                    + "|start_date:" + Batch.getInstance().getStartDate().getTime()
                    + "|status:" + Batch.getInstance().getBatchStatus().getStatus());
        }
    }

    public void addTask(Runnable r) {
        super.execute(r);
        LOG.debug("Task " + r.toString() + " added");
    }

    @Override
    public void terminated() {
        Batch.getInstance().setEndDate(Calendar.getInstance().getTime());
        Batch.getInstance().getBatchStatus().doEndStatus();
        // We need synchronized here because "+" operator is not thread safe
        synchronized (MyThreadPoolExecutor.class) {
            Logger.getLogger("STDOUT").log(Level.INFO, "batch:end|id:" + Batch.getInstance().getId()
                    + "|name:" + Batch.getInstance().getName()
                    + "|start_date:" + Batch.getInstance().getStartDate().getTime()
                    + "|end_date:" + Batch.getInstance().getEndDate().getTime()
                    + "|duration:" + Util.buildDurationFromDates(Batch.getInstance().getStartDate(),
                    Batch.getInstance().getEndDate())
                    + "|status:" + Batch.getInstance().getBatchStatus().getStatus());
        }
        super.terminated();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        // Ensure that Batch state is set to Batch.RUNNING
        if (Batch.getInstance().getBatchStatus().getStatus() != Batch.Status.RUNNING) {
            Batch.getInstance().getBatchStatus().setStatus(Batch.Status.RUNNING);
        }
    }

}
