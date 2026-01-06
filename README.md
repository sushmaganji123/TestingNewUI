<br/>
<div id="simplifyQA-logo" align="center">
    <br />
    <img src=".theia\readmeImage.png" alt="SimplifyQA Logo" width="900" height="290"/>
    <h2>🚀 SimplifyQA Code Editor IDE</h2>
</div>

<div id="badges" align="center">
    <p>The <b>SimplifyQA Code Editor IDE</b> is designed for a seamless coding experience and integrates effortlessly with <b>SimplifyQA</b>. ✨</p>
    <p>It also features an <b>inbuilt AI 🤖</b> that assists with coding, making development smoother, easier and more efficient.</p>
</div>

---

## 📝 How to Write the Code?

The **SimplifyQA Code-Editor** opens with a customized project tailored for you. Follow these steps to get started:

📂 **Project Navigation:**

- Navigate to the **project folder** and expand the structure.
- Go to **src/main/java/com/simplifyqa/codeeditor**.
- Refer to **SampleClass.java** for guidance.

🛠 **Coding Instructions:**

- Create **any number of packages and classes** inside `src/main/java/com/simplifyqa/codeeditor`.
- Use `@SyncAction` annotation to sync custom actions, so that they appear in the **SimplifyQA UI**.
- Maintain **unique IDs** for each custom action.
- Methods annotated with `@SyncAction` should be **public** and return a **boolean value**.
- Use `@FormulaBuilder` annotation to sync custom formula, so they appear in the **SimplifyQA UI**.
- Maintain **unique IDs and unique name** for each custom formula.
- Methods annotated with `@FormulaBuilder` should be **public**.
- Custom classes containing `@SyncAction or  @FormulaBuilder` methods **must** have a public default constructor.

⚡ **Code examples of custom action and formula:**

```java
@SyncAction(uniqueId = "sample-reverse-string", groupName = "Generic", objectTemplate = @ObjectTemplate(name = TechnologyType.GENERIC, description = "This action reverses a given string"), objectRequired = false)
    public boolean reverseString(String value) {
        if (value == null) {
            log.info("The provided string is null.");
            return false;
        }
        String reversed = new StringBuilder(value).reverse().toString();
        log.info("Original String: " + value + ", Reversed String: " + reversed);
        apiDriver.getExecutionLogReporter().info("reverseString executed with result: " + reversed);
        return true;
    }
```
```java
@FormulaBuilder(name = "CUSTOMCONCAT",uniqueId = "myunique",description = @Description(details = "This formula conacts",example = "=CUSTOMCONCAT(\"abcd\",\"efgh\")",parameters = {@Parameter(keyName = "firstKey",value = "abcd",dataType = ParameterDataType.STRING),@Parameter(keyName = "secondKey",value = "efgh",dataType = ParameterDataType.STRING)}))
public String testFormula(String value1, String value2){
        System.out.println("1st Formula param 1 is: "+value1);
        System.out.println("1st Formula param 2 is: "+value2);
        return value1+value2;
    }
```

💡 **Driver Auto-Injection:**
Use the following annotations to automatically inject the driver during runtime:

- `@AutoInjectWebDriver` 🌐 (Web Automation)
- `@AutoInjectAndroidDriver` 🤖 (Android Automation)
- `@AutoInjectIOSDriver` 🍏 (iOS Automation)
- `@AutoInjectApiDriver` 🔄 (API Automation)
- `@AutoInjectMainFrameDriver` 🖥 (Mainframe Automation)
- `@AutoInjectDesktopDriver` 💻 (Desktop Automation)
- `@AutoInjectSAPDriver` 🏭 (SAP Automation)


```java

@AutoInjectWebDriver
private IQAWebDriver driver;

@AutoInjectAndroidDriver
private IQAAndroidDriver androidDriver;
```

---
📌 **Additional Features:**

- `@AutoInjectCurrentObject` can be used to capture object attributes of the current step during test case recording.

```java

@AutoInjectCurrentObject
private SqaObject currentObject;
```

- To get the multiple objects or other than current step's object from object repository, refer the code below.

```java
 private SqaObject extractSqaObjectByName(String name) {
    Optional<SqaObject> sqaObjectFromRepo = driver.getSqaObjectFromRepo(name);
    return sqaObjectFromRepo.orElseThrow(RuntimeException::new);
}
```
- `@AutoInjectExecutionLogger` can be used to inject the execution logger directly instead of relying on driver.

```java
@AutoInjectExecutionLogger
private IExecutionLogReporter logReporter;
```

---
🛠️ **Building the jar:**
- Run **build.bat** (Windows) or **macBuild.sh** (Mac) to validate the code and build the JAR file.

⚡ **JAR Execution Options:**
During build, you’ll see the prompt:

```
DO YOU WANT TO USE LOCAL JAR PRESENT IN TARGET FOLDER FOR EXECUTION? ENTER Y (for yes) OR N (for no):
```

- Enter **Y** to use the local JAR file in the `target` folder for the execution.
- Enter **N** to download and use the latest cloud JAR automatically for the execution.

---
⚠️**Build Validation:**

- If `@SyncAction` methods contain **duplicate unique IDs**, the build process will **fail** and display an error
  message showing:
    - **Unique ID**
    - **Methods**
    - **Classes** where duplication occurred.

```
SEVERE: ------------------DUPLICATE UNIQUE ID's are found------------------
 INFO:    UniqueId: MyProject-Sample-002
 methods: customSampleTypeText and customSampleTypeText
 classes: com.simplifyqa.codeeditor.SampleClass and class com.simplifyqa.codeeditor.innnerpack.SampleClass
```

- If `@FormulaBuilder` methods contain **duplicate unique IDs or unique names**, the build process will **fail** and display an error
  message showing:
    - **Unique ID**
    - **Methods**
    - **Classes** where duplication occurred.

```
        --------------------------------DUPLICATE UNIQUE ID's are found in FORMULAS--------------------------------
Unique ID : CUSTOM_1_myunique
Methods   : testFormula and testFormulatwice
Classes   : SampleClass and SampleClass

        --------------------------------DUPLICATE Unique ID's found for FORMULAS--------------------------------
Duplicated Unique Id          : myunique
Unique Name in method         : SUM()
Unique Name in synced formula : CUSTOMSUM()
Method                        : formulaSum
Classe                        : SampleClass
```

---

🤖 **AI Assistance:**

- Use the inbuilt AI for **code suggestions** and **debugging**. It’s there to make your life easier! 🚀
- Use **Ctrl+L** to add code to chat as context 🖱️📋💬.
- Use **Ctrl+I** to generate code directly in the editor ✨👨‍💻🚀.

---

## 👨‍💻 Developers

👤 **Abhishek M Balegundi** - [abhishek.m@simplify3x.com](mailto:abhishek.m@simplify3x.com)

---

## 💡 Troubleshooting

⚡ **Best Practices:**

- Maintain **clean code** and eliminate syntax errors. ✅
- Contact **SimplifyQA Support Team** - [support@simplify3x.com](mailto:support@simplify3x.com) 📩

---

Happy Coding! 🎉🚀

