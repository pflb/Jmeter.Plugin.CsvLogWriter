Есть проект Jmeter-plugins.org, с **jp@gc - Flexible File Writer**:

- standard\src\kg\apc\jmeter\reporters:
  - FlexibleFileWriter.java
  - FlexibleFileWriterGui.java

Задача - сделать:

- вывод текстов ошибок;
- вывод в csv-формат;
- ротацию.


D:\Project\jmeter-plugins\standard\src\kg\apc\jmeter\reporters\\**FlexibleFileWriter.java**, ключевой метод тут ``public void sampleOccurred(SampleEvent evt)``:

	
	@Override
    public void sampleOccurred(SampleEvent evt) {
        if (fileChannel == null || !fileChannel.isOpen()) {
            if (log.isWarnEnabled()) {
                log.warn("File writer is closed! Maybe test has already been stopped");
            }
            return;
        }

        ByteBuffer buf = ByteBuffer.allocateDirect(writeBufferSize);
        for (int n = 0; n < compiledConsts.length; n++) {
            if (compiledConsts[n] != null) {
                //noinspection SynchronizeOnNonFinalField
                synchronized (compiledConsts) {
                    buf.put(compiledConsts[n].duplicate());
                }
            } else {
                if (!appendSampleResultField(buf, evt.getResult(), compiledFields[n])) {
                    appendSampleVariable(buf, evt, compiledVars[n]);
                }
            }
        }

        buf.flip();

        try {
            syncWrite(buf);
        } catch (IOException ex) {
            log.error("Problems writing to file", ex);
        }
    }


В методе ``sampleOccurred`` удобно реализовать обработку подразапросов.
Для получения текстов ошибок удобно выводить первые 200-300 символов ответа в лог, для запросов с кодом ответа == 500.

Ротацию логов можно реализовать в методе ``syncWrite``, выполняя ротацию по количеству записанных байт в один файл.
