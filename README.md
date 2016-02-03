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


**Описание функционала плагина pflb@CsvLogWriter.**

В ходе работы был написан плагин pflb@CsvLogWriter для JMeter. Данный плагин позволяет записывать подробный лог-файл 
без предварительных настроек. 
  Лог-файл фиксирует следующие данные: 
- timeStamp - начало обработки, мс;  
- elapsed - продолжительность обработки, мс;
- label - наименование компонента JMeter; 
- responseCode - код ответа на запрос; 
- responseMessage - содержание ответа на запрос;	
- threadName - наименование катушки;	
- dataType - тип данных;
- success - статус выполнения запроса;	
- failureMessage - сообщение об ошибке;
- bytes - объем данных ответа сервера;	
- grpThreads - количество активных виртуальных пользователей текущей группы;	
- allThreads - общее количество активных виртуальных пользователей всех групп; 
- URL - ссылка;	
- Filename - наименования файла, в который записываются ответы;	
- Latency - время до получения первого ответа сервера;	
- Encoding - кодировка;	
- SampleCount - количество семплов;	
- ErrorCount - количество ошибок;	
- Hostname - наименование машины;	
- IdleTime - время простоя, мс;
- Connect - время, затраченное на установку соединения;	
- responseData - полное содержание ошибки в случае ее возникновения;	
- transactionLevel - уровень транзакции.


  К ключевым особенностям данного плагина можно отнести то, что он может фиксировать результаты работы дочерних подзапросов и записывать полный текст ошибки, 
при ее возникновении, в виде обычного текста, а не в XML-формате. Так же, во избежание проблем с дальнейшей обработкой лог-файла, плагин поддерживает ротацию. 

Пример результата работы плагина:

    timeStamp;elapsed;label;responseCode;responseMessage;threadName;dataType;success;failureMessage;bytes;grpThreads;allThreads;URL;Filename;Latency;Encoding;SampleCount;ErrorCount;Hostname;IdleTime;Connect;"responseData";"transactionLevel"
    1454498796125;6759;"Transaction Controller1";"200";"Number of samples in transaction : 11, number of failing samples : 0";"Thread Group 1-1";"";true;"";1289;1;1;"null";"";0;"ISO-8859-1";1;0;"aperevozchikova";9;0;"";0
    1454498796125;540;"Transaction Controller 2";"200";"Number of samples in transaction : 1, number of failing samples : 0";"Thread Group 1-1";"";true;"";114;1;1;"null";"";0;"ISO-8859-1";1;0;"aperevozchikova";9;0;"";1
    1454498796126;540;"jp@gc - Dummy Sampler";"200";"OK";"Thread Group 1-1";"text";true;"";114;1;1;"null";"";4;"ISO-8859-1";1;0;"aperevozchikova";9;0;"";2
    1454498796667;667;"Transaction Controller 2";"200";"Number of samples in transaction : 1, number of failing samples : 0";"Thread Group 1-1";"";true;"";114;1;1;"null";"";0;"ISO-8859-1";1;0;"aperevozchikova";9;0;"";1
    1454498796668;667;"jp@gc - Dummy Sampler";"200";"OK";"Thread Group 1-1";"text";true;"";114;1;1;"null";"";9;"ISO-8859-1";1;0;"aperevozchikova";9;0;"";2
    1454498797336;666;"Transaction Controller 2";"200";"Number of samples in transaction : 1, number of failing samples : 0";"Thread Group 1-1";"";true;"";114;1;1;"null";"";0;"ISO-8859-1";1;0;"aperevozchikova";9;0;"";1
    1454498802041;853;"jp@gc - Error Sampler";"500";"Internal Server Error";"Thread Group 1-1";"text";true;"";149;1;1;"null";"";87;"ISO-8859-1";1;0;"aperevozchikova";9;0;"Error text."

Для запуска плагина необходимо заполнить поля Filename и Rotation. 
Поле Filename содержит путь к файлу, в котором будет вестись фиксация результатов работы. Можно прописать директорию вручную, или выбрать файл используя кнопку Browse.
Поле Rotation содержит число строк, при достижении которого будет создан новый лог-файл. Наименование нового лог-файла формируется добавлением постфикса с номером лог-файла к оригинальному наименованию.
По умолчанию, поле Rotation заполнено значением 100000. При очищении данного поля будет использована величина, заданная по умолчанию.