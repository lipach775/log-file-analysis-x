## 日志文件分析，提取日志行上下到表格中
<img width="1363" alt="image" src="https://github.com/user-attachments/assets/cbe653e8-1e67-4c62-a062-5428a1df1357" />
<img width="1363" alt="image" src="https://github.com/user-attachments/assets/0cacafe2-e81e-4d2e-98c7-22248103e41e" />

## 运行结果
<img width="1203" alt="image" src="https://github.com/user-attachments/assets/e646b4b6-a0f5-4338-87bf-ed3a69d000c6" />

## 列解析方法配置说明
       /**
         * 列内容解析方法配置，和分析后的表头数量一致
         *
         * 通用参数：
         * * :RemoveNT[true]/:RemoveNT  移除结果列中的换行符、制表符
         *
         * 公共解析方法名称及参数：
         * * 获取日志行内容
         * * 获取线程名
         * * 计算两个时间之间的差值(ms)
         *      :Date1[${0}]/:Date1
         *      :Date2[${1}]/:Date2
         *      :Format[yyyy-MM-dd HH:mm:ss:SSS]/:Format
         * * 获取当前行之后，下一个满足条件的日志行
         *      :And[${0},output]/:And
         *      :Or[]/:Or
         * * 获取当前行之前，上一个满足条件的日志行
         *      :And[${0},output]/:And
         *      :Or[]/:Or
         * * 按字符串开始结束截取字符串
         *      :Input[${0}]/:Input
         *      :StartWithString[abc]/:StartWithString
         *      :EndWithString[efg]/:EndWithString
         * * 按下标开始结束截取字符串
         *      :Input[${0}]/:Input
         *      :StartWithIndex[2]/:StartWithIndex
         *      :EndWithIndex[5]/:EndWithIndex
         * * JSONPath提取内容
         *      :Input[${0}]/:Input
         *      :JSONPath[$.a]/:JSONPath
         * * 正则提取内容
         *      :Input[${0}]/:Input
         *      :Regex[TN.*\\(String\\)]/:Regex
         */
