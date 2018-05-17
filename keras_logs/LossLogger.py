from keras.callbacks import Callback

class LossLogger(Callback):
    """Callback that logs accuracy to a text file.
    # Arguments
        count_mode: One of "steps" or "samples".
            Whether the progress bar should
            count samples seen or steps (batches) seen.
        stateful_metrics: Iterable of string names of metrics that
            should *not* be averaged over an epoch.
            Metrics in this list will be logged as-is.
            All others will be averaged over time (e.g. loss, etc).
    # Raises
        ValueError: In case of invalid `count_mode`.
    """

    def __init__(self, logFileName):
        super(LossLogger, self).__init__()
        self.seen = 0
        self.logFileName = logFileName
        self.logFile = open(self.logFileName, 'w')

    def on_batch_end(self, batch, logs=None):
        logs = logs or {}
        self.seen += 1
        print(self.seen, logs.get('loss'), file=self.logFile) # logs.get('acc') for accuracy.


    def on_train_end(self, logs=None):
        self.logFile.close()

