<template>
  <div class="form-container">
    <form>
      <h2>日志文件分析</h2>
      <!-- 输入参数 -->
      <h3>输入参数</h3>
      <div>
        <div class="config-title"><el-tag>输入文件路径（多个文件内容会按顺序拼接）</el-tag><el-button type="primary" size="small" @click="addInputPath">添加</el-button></div>
        <draggable v-model="form.inputParamsConfig.inputFilePaths" @end="updateOrder" class="draggable-list" item-key="index">
          <template #item="{ element, index }">
            <div class="input-group">
              <el-input v-model="form.inputParamsConfig.inputFilePaths[index]" placeholder="输入文件路径"></el-input>
              <el-button type="danger" @click="removeInputPath(index)">删除</el-button>
            </div>
          </template>
        </draggable>
      </div>

      <div>
        <div class="config-title"><el-tag>有效日志行过滤(为了节省内存占用) And 过滤器</el-tag><el-button type="primary" size="small" @click="addMatcher('inputFileRegexMatcherAnd')">添加</el-button></div>
        <draggable v-model="form.inputParamsConfig.inputFileRegexMatcherAnd" @end="updateOrder" class="draggable-list" item-key="index">
          <template #item="{ element, index }">
            <div class="input-group">
              <el-input v-model="form.inputParamsConfig.inputFileRegexMatcherAnd[index]" placeholder="And 正则"></el-input>
              <el-button type="danger" @click="removeMatcher('inputFileRegexMatcherAnd', index)">删除</el-button>
            </div>
          </template>
        </draggable>
      </div>

      <div>
        <div class="config-title"><el-tag>有效日志行过滤(为了节省内存占用) Or 过滤器</el-tag><el-button type="primary" size="small" @click="addMatcher('inputFileRegexMatcherOr')">添加</el-button></div>
        <draggable v-model="form.inputParamsConfig.inputFileRegexMatcherOr" @end="updateOrder" class="draggable-list" item-key="index">
          <template #item="{ element, index }">
            <div class="input-group">
              <el-input v-model="form.inputParamsConfig.inputFileRegexMatcherOr[index]" placeholder="Or 正则"></el-input>
              <el-button type="danger" @click="removeMatcher('inputFileRegexMatcherOr', index)">删除</el-button>
            </div>
          </template>
        </draggable>
      </div>

      <!-- 输出参数 -->
      <h3>输出参数</h3>
      <div>
        <div class="config-title"><el-tag>输出文件路径（支持.xlsx、.txt等）</el-tag></div>
        <div class="draggable-list">
          <div class="input-group">
            <el-input  v-model="form.outputParamsConfig.outputFilePath" placeholder="输出文件路径"></el-input>
          </div>
        </div>
      </div>

      <div>
        <div class="config-title"><el-tag>日志索引行匹配规则</el-tag><el-button type="primary" size="small" @click="addOutputLineRegexMatcherAnd">添加</el-button></div>
        <draggable v-model="form.outputParamsConfig.lineRegexMatcherAnd" @end="updateOrder" class="draggable-list" item-key="index">
            <template #item="{ element, index }">
              <div class="input-group">
                <el-input v-model="form.outputParamsConfig.lineRegexMatcherAnd[index]" placeholder="输入日志索引行匹配规则"></el-input>
                <el-button type="danger" @click="removeOutputLineRegexMatcherAnd(index)">删除</el-button>
              </div>
            </template>
        </draggable>
      </div>

      <div>
        <div class="config-title"><el-tag>列名（在列分析方法参数中可使用${0}、${1}的方式引用对应列的值）</el-tag><el-button type="primary" size="small" @click="addHeader">添加</el-button></div>
        <draggable v-model="form.outputParamsConfig.tableHeaderList" @end="updateOrder" class="draggable-list" item-key="index">
          <template #item="{ element, index }">
            <div class="input-group">
              <el-input v-model="form.outputParamsConfig.tableHeaderList[index]" :placeholder="'${' + index + '} 列名'"></el-input>
              <el-button type="danger"  @click="removeHeader(index)">删除</el-button>
            </div>
          </template>
        </draggable>
      </div>

      <div>
        <div class="config-title"><el-tag>列分析方法（列分析方法按顺序处理对应下标的列）</el-tag><el-button type="primary" size="small" @click="addHeader">添加</el-button></div>
        <draggable v-model="form.outputParamsConfig.tableColumnDealMethodConfigList" @end="updateOrder" class="draggable-list" item-key="index">
          <template #item="{ element, index }">
            <div class="input-group">
              <el-autocomplete
                  v-model="form.outputParamsConfig.tableColumnDealMethodConfigList[index]"
                  :fetch-suggestions="queryCommonColumnDealMethods"
                  clearable
                  class="inline-input w-50"
                  :placeholder="'$(' + index + ') 列分析方法'"
              />
              <el-button type="danger"  @click="removeHeader(index)">删除</el-button>
            </div>
          </template>
        </draggable>
      </div>

      <div>
        <div class="config-title"><el-tag>结果输出列顺序（配置有效列顺序，数字代表列名中的第几列，为空则默认按列名顺序）</el-tag><el-button type="primary" size="small" @click="addResultShowColumnAndOrder">添加</el-button></div>
        <draggable v-model="form.outputParamsConfig.resultShowColumnAndOrder" @end="updateOrder" class="draggable-list" item-key="index">
          <template #item="{ element, index }">
            <div class="input-group">
              <el-input v-model="form.outputParamsConfig.resultShowColumnAndOrder[index]" :placeholder="`有效结果列输出顺序`"></el-input>
              <el-button type="danger"  @click="removeResultShowColumnAndOrder(index)">删除</el-button>
            </div>
          </template>
        </draggable>
      </div>

      <!-- Submit Button -->
      <el-button style="margin-top: 10px" @click="readConfig">读取配置</el-button>
      <el-button type="primary" style="margin-top: 10px" @click="submitForm('save')">保存配置</el-button>
      <el-button type="primary" style="margin-top: 10px" @click="submitForm('submit')">运行配置</el-button>
    </form>


    <el-drawer v-model="data.showHistoryConfig" :show-close="false">
        <div class="showHistoryConfig-container">
          <div class="showHistoryConfig-item" v-for="(item, index) in data.historyConfigs" :key="index">
            <div class="input-group">
              <el-input v-model="item.name"></el-input>
              <el-button type="success" @click="useHistoryConfig(item)">使用</el-button>
              <el-button type="danger" style="margin-left: 0" @click="deleteHistoryConfig(item, index)">删除</el-button>
            </div>
          </div>
        </div>
    </el-drawer>
  </div>
</template>

<script>
import { ref } from 'vue';
import draggable from 'vuedraggable';

import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  components: { draggable },
  setup() {
    const commonColumnDealMethods = ref([
      {value: "获取日志行内容"},
      {value: "获取线程名"},
      {value: "计算两个时间之间的差值(ms):Date1[${0}]/:Date1:Date2[${1}]/:Date2:Format[yyyy-MM-dd HH:mm:ss:SSS]/:Format"},
      {value: "获取当前行之后，下一个满足条件的日志行:And[${0},output]/:And:Or[]/:Or"},
      {value: "获取当前行之前，上一个满足条件的日志行:And[${0},output]/:And:Or[]/:Or"},
      {value: "按字符串开始结束截取字符串:Input[${0}]/:Input:StartWithString[abc]/:StartWithString:EndWithString[efg]/:EndWithString"},
      {value: "按下标开始结束截取字符串:Input[${0}]/:Input:StartWithIndex[2]/:StartWithIndex:EndWithIndex[5]/:EndWithIndex"},
      {value: "JSONPath提取内容:Input[${0}]/:Input:JSONPath[$.a]/:JSONPath"},
      {value: "正则提取内容:Input[${0}]/:Input:Regex[TN.*\\(String\\)]/:Regex"},
    ]);

    const form = ref({
      inputParamsConfig: {
        inputFilePaths: ['/Users/lipancheng/Desktop/code/LogFileAnalysisX/src/main/java/org/lpc/logfileanalysisx/testLogFile/test1.log'],
        inputFileRegexMatcherAnd: [],
        inputFileRegexMatcherOr: [],
      },
      outputParamsConfig: {
        lineRegexMatcherAnd: ['日志', 'input'],
        outputFilePath: './src/main/java/org/lpc/logfileanalysisx/testLogFile/结果.txt',
        resultShowColumnAndOrder: ['1', '3', '4', '5', '2'],
        tableColumnDealMethod: {},
        tableColumnDealMethodConfig: {
          // 0: '获取日志行内容',
          // 1: '获取线程名',
          // 2: '获取当前行之后，下一个满足条件的日志行:And[${1},output]/:And:Or[]/:Or',
          // 3: '按下标开始结束截取字符串:Input[${0}]/:Input:StartWithIndex[0]/:StartWithIndex:EndWithIndex[23]/:EndWithIndex',
          // 4: '按下标开始结束截取字符串:Input[${2}]/:Input:StartWithIndex[0]/:StartWithIndex:EndWithIndex[23]/:EndWithIndex',
          // 5: '计算两个时间之间的差值(ms):Date1[${3}]/:Date1:Date2[${4}]/:Date2:Format[yyyy-MM-dd HH:mm:ss:SSS]/:Format',
        },
        tableColumnDealMethodConfigList: [
          '获取日志行内容',
          '获取线程名',
          '获取当前行之后，下一个满足条件的日志行:And[${1},output]/:And:Or[]/:Or',
          '按下标开始结束截取字符串:Input[${0}]/:Input:StartWithIndex[0]/:StartWithIndex:EndWithIndex[23]/:EndWithIndex',
          '按下标开始结束截取字符串:Input[${2}]/:Input:StartWithIndex[0]/:StartWithIndex:EndWithIndex[23]/:EndWithIndex',
          '计算两个时间之间的差值(ms):Date1[${3}]/:Date1:Date2[${4}]/:Date2:Format[yyyy-MM-dd HH:mm:ss:SSS]/:Format',
        ],
        tableHeader: {
          // 0: '发送请求日志',
          // 1: '线程名',
          // 2: '结束日志',
          // 3: '请求开始时间',
          // 4: '请求结束时间',
          // 5: '请求耗时(ms)',
        },
        tableHeaderList: [
            '发送请求日志',
            '线程名',
            '结束日志',
            '请求开始时间',
            '请求结束时间',
            '请求耗时(ms)'
        ],
      },
    });

    const addInputPath = () => form.value.inputParamsConfig.inputFilePaths.push('');
    const removeInputPath = (index) => form.value.inputParamsConfig.inputFilePaths.splice(index, 1);
    const addOutputLineRegexMatcherAnd = () => form.value.outputParamsConfig.lineRegexMatcherAnd.push('');
    const removeOutputLineRegexMatcherAnd = (index) => form.value.outputParamsConfig.lineRegexMatcherAnd.splice(index, 1);
    const addMatcher = (type) => form.value.inputParamsConfig[type].push('');
    const removeMatcher = (type, index) => form.value.inputParamsConfig[type].splice(index, 1);
    const addHeader = () => {
      form.value.outputParamsConfig.tableHeaderList.push('');
      form.value.outputParamsConfig.tableColumnDealMethodConfigList.push('');
    };
    const removeHeader = (index) => {
      form.value.outputParamsConfig.tableHeaderList.splice(index, 1);
      form.value.outputParamsConfig.tableColumnDealMethodConfigList.splice(index, 1);
    };
    const addResultShowColumnAndOrder = () => {
      form.value.outputParamsConfig.resultShowColumnAndOrder.push('');
    };
    const removeResultShowColumnAndOrder = (index) => {
      form.value.outputParamsConfig.resultShowColumnAndOrder.splice(index, 1);
    };
    const updateOrder = (event) => {
      console.log("排序更新", event);
      // 可在此处添加逻辑处理拖拽排序结果
    };

    const generateUUID = () => {
      return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, (c) =>
          (c ^ (crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))).toString(16)
      );
    };

    const submitForm = async (type) => {
      try {
        // 处理数组为map
        form.value.outputParamsConfig.tableHeader = {};
        form.value.outputParamsConfig.tableColumnDealMethodConfig = {};

        if (form.value.outputParamsConfig.tableHeaderList) {
          let tableHeaderMap = {};
          for (let i = 0; i < form.value.outputParamsConfig.tableHeaderList.length; i++) {
            tableHeaderMap[i + ""] = form.value.outputParamsConfig.tableHeaderList[i];
          }
          form.value.outputParamsConfig.tableHeader = tableHeaderMap;
        }
        if (form.value.outputParamsConfig.tableColumnDealMethodConfigList) {
          let tableColumnDealMethodConfigMap = {};
          for (let i = 0; i < form.value.outputParamsConfig.tableColumnDealMethodConfigList.length; i++) {
            tableColumnDealMethodConfigMap[i + ""] = form.value.outputParamsConfig.tableColumnDealMethodConfigList[i];
          }
          form.value.outputParamsConfig.tableColumnDealMethodConfig = tableColumnDealMethodConfigMap;
        }

        if (type === 'save') {
          ElMessageBox.prompt('请输入一个名称', '保存', {
            confirmButtonText: '保存',
            cancelButtonText: '取消',
            inputPattern: /^(?=.*\S).*$/,
            inputErrorMessage: '输入内容不能为空',
          })
              .then(async ({value}) => {

                let param = {
                  id: generateUUID(),
                  name: value,
                  config: form.value
                }
                console.log('保存Request:', JSON.stringify(param));
                const response = await fetch('/api/save', {
                  method: 'POST',
                  headers: {'Content-Type': 'application/json'},
                  body: JSON.stringify(param),
                });
                console.log('保存Response:', await response.text());
              });
        } else {
          let param = form.value;
          console.log('运行Request:', JSON.stringify(param));
          const response = await fetch('/api/submit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(param),
          });

          if (response.ok) {
            // 获取文件流
            const blob = await response.blob();
            const filename = decodeURIComponent(
                response.headers.get('Content-Disposition').split('filename*=UTF-8\'\'')[1]
            );

            // 创建下载链接
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
            console.log('文件已下载:', filename);
          } else {
            console.error('保存失败:', await response.text());
          }
        }

      } catch (error) {
        console.error('Error submitting form:', error);
      }
    };

    const readConfig = async () => {
      data.value.showHistoryConfig = true;
      let param = {};
      console.log('查询配置Request:', JSON.stringify(param));
      const response = await fetch('/api/queryConfig', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(param),
      });
      let responseData = await response.json();
      console.log('查询配置Response:', responseData);
      data.value.historyConfigs = JSON.parse(JSON.stringify(responseData));
    };

    const createFilter = (queryString) => {
      return (method) => {
        return method.value.toLowerCase().includes(queryString.toLowerCase());
      };
    };

    const queryCommonColumnDealMethods = (queryString, cb) => {
      const results = queryString
          ? commonColumnDealMethods.value.filter(createFilter(queryString))
          : commonColumnDealMethods.value;
      cb(results);
    };

    const useHistoryConfig = (item) => {
      form.value = JSON.parse(JSON.stringify(item.config));
      data.value.showHistoryConfig = false;
      console.log("使用配置：" + JSON.stringify(item.config));
    };

    const deleteHistoryConfig = async (item) => {
      console.log("删除配置：" + JSON.stringify(item.config));
      let param = {
        id: item.id
      };
      console.log('删除配置Request:', JSON.stringify(param));
      const response = await fetch('/api/deleteConfig', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(param),
      });
      let responseData = await response.json();
      console.log('删除配置Response:', responseData);
      data.value.historyConfigs = JSON.parse(JSON.stringify(responseData));
    };

    const data = ref({
      showHistoryConfig: false,
      historyConfigs: [
          // {
          //   name: "测试",
          //   config: {}
          // }
      ],
    });

    return {
      form,
      addInputPath,
      removeInputPath,
      addOutputLineRegexMatcherAnd,
      removeOutputLineRegexMatcherAnd,
      addMatcher,
      removeMatcher,
      addHeader,
      removeHeader,
      addResultShowColumnAndOrder,
      removeResultShowColumnAndOrder,
      updateOrder,
      submitForm,
      readConfig,
      queryCommonColumnDealMethods,
      useHistoryConfig,
      deleteHistoryConfig,
      data,
    };
  },
};
</script>

<style>
.form-container {
  min-width: 800px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 10px;
  background-color: #f9f9f9;
}
h2 {
  text-align: center;
  margin-bottom: 20px;
}
.input-group {
  display: flex;
  gap: 10px;
  margin-top: 5px;
  margin-bottom: 5px;
}

.draggable-list {
  padding: 10px;
  border: 1px dashed #aaa;
  border-radius: 5px;
  background: #fff;
}
.config-title {
  width: 100%;
  height: 40px;
  line-height: 40px;
  text-align: left;
  margin-top: 10px;
}
.el-drawer__header {
  display: none !important;
}
</style>