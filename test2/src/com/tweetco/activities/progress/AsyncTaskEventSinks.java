package com.tweetco.activities.progress;

public interface AsyncTaskEventSinks
{
    public interface AsyncTaskCancelCallback
    {
        public void onCancelled();
    }
    
    public interface UIEventSink
    {
        /**
         * Callback to indicate asynctask has started and cancel callback. If callback is null cancellation not supported.
         * If isShowProgressDialog is false, then the progress dialog will not be shown.
         * @param asyncTask
         * @param cancelCallback
         */
        public void onAysncTaskPreExecute(Object asyncTask,AsyncTaskCancelCallback cancelCallback, boolean isShowProgressDialog);
        
        /**
         * Callback to indicate that a message needs to be changed on whatever progress mechanism is being used
         */
        public void onUpdateProgressMessage(String message);
        
        public void onUpdateDataProgessValue(Integer updateProgress);
        
        public void onDataAsyncTaskCancelRequest(); // This is made public as this must be called directly by Service..
    }
}
