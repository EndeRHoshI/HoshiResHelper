# 资源管理助手
在公司一直负责多语言翻译，因为适配语言较多，加上项目管理不善，导致每次更新字符串都很痛苦，我一直深受其害。起初直接用公司写的一个用于导出导入字符串的 Java 小工具来处理，后来觉得这个小工具很多问题，可读性也很差，于是自己写了 [HoshiResUtils](https://github.com/EndeRHoshI/HoshiResUtils)。但是后来觉得还是不好用，最好能有图形化界面直接运行，所以打算用 Kotlin 写一个 Mac/Win 桌面端工具来自动化处理

除了多语言管理，开发中我们还会遇到一些需要批量处理资源的场景，例如统一整理 drawable 图片，这里也把这个功能合并做进来

下面这些是直接复制之前项目的 README，后续开发好功能后，再进行适当调整即可
## drawable 图片资源管理工具
### 场景描述
有时候我们需要精简 apk 内容，缩小 apk 包大小，要删掉一些不需要的 drawable 下的图片，经过一些调查，发现当前最省事的一个做法是，只留下 xxhdpi 的图片，详见[文章](https://www.jianshu.com/p/0eb2824d6011)，这样，就可以写一些代码，直接整理 drawable 文件夹，只留下 xxhdpi 的文件了；同样的，如果 xxhdpi 中没有，其它的尺寸下有的话，也只保留最大的尺寸的那一份
### 开发思路
1. 遍历 res 文件夹内的子文件夹，找出 drawable-xxhdpi 之类的文件夹（drawable 文件夹排除在外）
2. 根据定义好的各个尺寸的优先级，从高到低以其作为基准（这里默认最高优先级是 drawable-xxhdpi），将其它 drawable 文件夹中有重复的删除，不重复的保留（举例子，执行到最高的 drawable-xxhdpi 时，其余尺寸文件夹中的同名文件将被删除，然后又轮到 drawable-xhdpi，其余文件夹中同名的文件又被删除，以此类推）
3. 这里为什么引入优先级的概念，是因为处理低优先级的尺寸时可以直接跳过已经处理过的高优先级尺寸，节省一些时间（虽然也没多大变化），如果想再简化一点的话，可以直接用顺序来表示优先级，直接按顺序取基准尺寸来处理，反正以低优先级尺寸作为基准去删除高优先级尺寸的文件时，高优先级尺寸中已经没有同名文件了（处理高优先级时已经去掉其它尺寸的所有同名文件了），结果也是差不多的，不需要额外做处理
### 使用步骤
1. 打开 [DrawableMgr](src/main/kotlin/drawable/DrawableMgr.kt)，填入需要整理的 res 文件夹路径
2. 在 `DensityQualifier` 密封类中配置好各个尺寸的优先级
3. 调整 `getDensityQualifierList()` 方法，配置好需要处理的各个尺寸（有些尺寸有些时候不需要处理，如 nodpi、anydpi 这些）
4. 运行 main 方法，这样所有的 drawable 文件夹内就会得到唯一的一份图片资源
5. 最后可以和 UI 协调，把所有低尺寸文件都出一份 drawable-xxhdpi 的，然后统一放到 drawable-xxhdpi 中，最后把其它低尺寸的文件删掉
## string.xml 多语言字符串管理工具
### 场景描述
在做多语言适配时，我们需要提供应用内的文案给翻译人员进行翻译，你不能直接把整个应用的 string.xml 打包扔给对方，一般来说，将项目内的 string.xml 导出为 Excel 文件再交给翻译人员会比较好
### 开发思路
1. 直接遍历找到整个项目中符合正则匹配的 `*string*.xml`、`*array*.xml` 的文件，string 和 array 前后可以有其它字符，但是一定要以 .xml 为结尾
2. 找到后进行分组，每个组里面以 values 为基础，其余的 values-xxx 则是其它的语种（这里进行分组是因为有时候我们模块化开发会有不同的模块，多渠道发布有不同的渠道，多版本定制化开发会有不同的 app 版本，这样会导致不止一组 string.xml 存在）
3. 分好组后，从所有 string.xml 中读取键值对，以 values 下的作为基准，导出到 Excel 中，values 中没有的，直接忽略掉
4. xml 转换成 excel 时，先忽略掉 translatable = false 的，然后 translatable = true 的视为需要翻译，再对比需要翻译语言的目标 xml，看是否有这个项，如果没有这一项，则视为最终需要翻译
### 使用步骤
1. 输入项目路径和参数，控制是全量输出、已翻译完全的还是未翻译完全的
2. 直接遍历找到整个项目中符合正则匹配的 `*string*.xml`、`*array*.xml` 的文件
3. 以 string.xml 和 array.xml 作为基准进行判断已翻译还是未翻译，多出的忽略不理
4. 生成一个 excel，每个文件放一个工作表，每种语言一个列，这样从 xml 到 excel 就完成了
5. 输入 excel 路径和参数，控制是全量输出、已翻译的还是未翻译的
6. 读取 excel，逐个工作表去读取，逐行逐列读取，转换回 xml，然后就可以把 xml 放回到项目中

## 开发流程
1. 首先编写代码实现功能，本地运行调试
2. 测试稳定后，推送到 Github
3. 触发 Action 进行自动化构建
4. 取得 Mac/Win 下的产物（不知道 Action 能不能打，如果不能的话，这一步可能换成本地打包，现在已知 Mac 环境下是不能打包 Win 的 Msi 包的）
5. 分发给同事使用

### 其它事项
#### 开发时
1. 新的字符串直接写入 string.xml，且用中文书写
2. 不需要翻译的要标记 translatable = false
#### 翻译前
交给负责翻译的人员前，可以对 Excel 进行一些处理，以免翻译人员进行翻译时出错
1. 标注需要翻译的列，或者把不需要翻译的列删除掉
2. 告知翻译人员，部分文案存在占位符，占位符只用于占位，不需要翻译
#### 翻译后
拿到已经翻译的 Excel，转换成对应的 xml，只需要将转换好的各个语言的 xml 复制好，再粘贴到项目对应的各个语言的 xml 的下方增量更新即可，如果后续发现有条目不对，可以直接删除，再导出一遍即可

同时要注意以下问题：
1. xml -> Excel 时，会把转义字符去掉，Excel -> xml 时，要留意是否要再加上转义字符，特别是要留意意大利语和法语，他们的语言中经常带有 `'` 字符，会导致 xml 报错，需要加上 `\` 转义
2. RTL 语系，要注意以下：
  1. 你要确保你的手机也打开 RTL 布局开关，可以在开发者模式里面进行设置
  2. 处理格式化占位符（%s、%d 等）时，编译可能不会报错，但是到了特定页面无法正确处理占位符（读成了s%）而导致闪退，实践中发现好像如果被夹在希伯来语中间就需要改成 s%，如果不是则不用，还需要再测试一下，最简单直接的处理手段：直接删掉，然后手打 %s，让他自己排版；除了格式化占位符问题，RTL 布局也是个问题，这个要做得很好，应该需要 UI 介入，是题外话了
  3. 在设置 gravity 时，要记得设置水平方向的 gravity（忘记为什么了，后续遇到可以再说明下），使用 left、right 相关属性时，用 start、end 替代
  4. RecyclerView 的 reverseLayout 参数可以根据是否 RTL 布局来进行设置，如果整个页面都不需要 RTL，可以配置 `android:layoutDirection="ltr"`
  5. 注意某些软件打开 Excel 表格，下方的表格框渲染不一定是正确的，它有可能是反过来的，正确的书写方向要看上方的文本编辑框才行，如果实在不能确认的话，可以复制文本，粘贴到浏览器地址栏看看，或者直接 Google 翻译一下看是否正确。如果发现 TextView 的内容怎么都不正确，可以尝试将 TextView 的 textDirection 改成 anyRTL 再试试
  6. 可以看下相关文章：[Android 本地化适配：RTL（right-to-left） 适配清单 ](https://www.cnblogs.com/plokmju/p/android_rtl.html)
3. Span 的高亮文案，翻译不当会导致不能正确高亮，高亮翻译时，可以在全文中使用不会被翻译到的字符（如阿拉伯数字）代替需要高亮的部分，翻译好高亮部分再套进去即可
4. 阿拉伯语言环境下进行数字格式化，会将原来的数字符号转化为阿拉伯文数字，引发各种问题，关于阿拉伯文数字，可看 [wiki](https://zh.wikipedia.org/wiki/%E9%98%BF%E6%8B%89%E4%BC%AF%E6%96%87%E6%95%B0%E5%AD%97)，想要规避的话，将格式化的环境固定为英文环境即可（`SimpleDateFormat(pattern, Locale.ENGLISH)`）
5. 土耳其语的 I 转换小写会变成 ı，没有了那一点，会导致 equals 或者 startWith 这种判断达不到预期效果，可以将格式化的环境固定为英文环境（使用 `.toLowerCase(Locale.ENGLISH)` 或 `.toLowerCase(Locale.US)` 来解决，或者使用 `.toLowerCase(Locale.ROOT)`，还可以使用 `.equalsIgnoreCase(xxx)` 来处理），可参考[文章](https://juejin.cn/post/6844903749094211592)
6. 文案最好要有相关人员审核，否则会出现各种问题
  1. 机翻不通顺是最常见的
  2. 如果是客户自愿提供翻译的，有些内容错误无法把关（漏翻译、或者错误翻译）
7. 文案的版本管理要注意，举个例子，送出去翻译后，改了一些文案，翻译回来后，又没及时更新，把旧的文案又覆盖上去了

### 参考项目
大牛做的相关系统：[国际化翻译系统V2正式上线了](https://mp.weixin.qq.com/s/M8bTBqstag3ioJg-1DjtOw)

## README from Google
下面这个是 Google 自动生成的 README，保留一下以供参考

This is a Kotlin Multiplatform project targeting Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…