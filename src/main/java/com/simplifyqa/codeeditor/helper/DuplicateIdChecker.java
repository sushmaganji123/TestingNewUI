package com.simplifyqa.codeeditor.helper;

import com.simplifyqa.codeeditor.plugin.CodeEditorPlugin;
import com.simplifyqa.pluginbase.codeeditor.annotations.FormulaBuilder;
import com.simplifyqa.pluginbase.codeeditor.annotations.SyncAction;
import com.simplifyqa.pluginbase.plugin.sync.models.FormulaData;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DuplicateIdChecker {
    private static final Logger logger = Logger.getLogger(DuplicateIdChecker.class.getName());
    private static final Map<String, Method> methodsList = new HashMap<>();
    private static final Map<String, Method> formulaList = new HashMap<>();
    private static final Map<String, Method> formulaNamesList = new HashMap<>();
    private static boolean buildStatus = true;
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String CUSTOM="CUSTOM";

    public static void main(String[] args) throws Exception {
        String packageName = "com.simplifyqa.codeeditor";
        List<Class<?>> classes = CustomPackageScanner.getClasses(packageName);
        logger.info("\u001B[1m\u001B[32m" + "[INFO]" + "\u001B[0m"+"Fetching Unique Ids from classes: " + classes);
        registerMethodsFromClass(classes);
        if (!buildStatus) {
            printErrorMessage();
            System.exit(1);
        }
        printSuccessMessage();
        System.exit(0);
    }

    private static void registerMethodsFromClass(List<Class<?>> classes) {
        String projectId=CodeEditorPlugin.projectId;
        List<FormulaData> allFormulas = new ArrayList<>();
        try {
            allFormulas = FormulaFetcher.fetchFormulas(projectId);
        } catch (Exception e) {
            System.out.println();
            System.out.println("\u001B[31;1m" + "\t--------------------------------EXCEPTION OCCURRED--------------------------------" + "\u001B[0m");
            System.out.println();
            System.out.println("\u001B[36;1m" + "MESSAGE    : " + "\u001B[0m" + "\u001B[33;1m" + e.getMessage() + "\u001B[0m");
            if (e.getMessage().contains("Connection refused")) {
                System.out.println();
                System.out.println("\u001B[36;1m" + "SUGGESTION : " + "\u001B[0m" + "\u001B[33;1m" + "Kindly check your internet connection and make sure Agent is turned ON" + "\u001B[0m");
            }
            buildStatus=false;
            return;
        }

        for (Class<?> eachClass : classes) {
            for (Method method : eachClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SyncAction.class)) {
                    if (!Modifier.isPublic(method.getModifiers())){
                        logger.log(Level.SEVERE, "\u001B[31m" + "METHOD: "+method.getName()+" is not public, but is annotated with @SyncAction, Please make it public." + "\u001B[0m");
                        buildStatus=false;
                    }
                    SyncAction annotation = method.getAnnotation((SyncAction.class));
                    String uniqueId = annotation.uniqueId();
                    if (methodsList.containsKey(uniqueId)) {
                        Method duplicateMethodInMap = methodsList.get(uniqueId);
                        System.out.println();
                        System.out.println("\u001B[31;1m" + "\t--------------------------------DUPLICATE UNIQUE ID's are found in ACTIONS--------------------------------" + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Unique ID : " + "\u001B[0m" + "\u001B[33;1m" + uniqueId + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Methods   : " + "\u001B[0m" + "\u001B[33;1m" + duplicateMethodInMap.getName() + "\u001B[0m" + " and " + "\u001B[33;1m" + method.getName() + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Classes   : " + "\u001B[0m" + "\u001B[33;1m" + duplicateMethodInMap.getDeclaringClass().getSimpleName() + "\u001B[0m" + " and " + "\u001B[33;1m" + method.getDeclaringClass().getSimpleName() + "\u001B[0m");
                        buildStatus = false;
                    } else {
                        methodsList.put(uniqueId, method);
                    }
                }else if (method.isAnnotationPresent(FormulaBuilder.class)) {
                    if (!Modifier.isPublic(method.getModifiers())){
                        logger.log(Level.SEVERE, "\u001B[31m" + "METHOD: "+method.getName()+" is not public, but is annotated with @SyncAction, Please make it public." + "\u001B[0m");
                        buildStatus=false;
                    }
                    FormulaBuilder annotation = method.getAnnotation(FormulaBuilder.class);
                    String uniqueId = CUSTOM+"_"+projectId+"_"+annotation.uniqueId();
                    String name = annotation.name();
                    if (!name.endsWith("()")) {
                        name=name+"()";
                    }
                    if (formulaList.containsKey(uniqueId)) {
                        Method duplicateMethodInMap = formulaList.get(uniqueId);
                        System.out.println();
                        System.out.println("\u001B[31m" + "\t--------------------------------DUPLICATE UNIQUE ID's are found in FORMULAS--------------------------------" + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Unique ID : " + "\u001B[0m" + "\u001B[33;1m" + uniqueId + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Methods   : " + "\u001B[0m" + "\u001B[33;1m" + duplicateMethodInMap.getName() + "\u001B[0m" + " and " + "\u001B[33;1m" + method.getName() + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Classes   : " + "\u001B[0m" + "\u001B[33;1m" + duplicateMethodInMap.getDeclaringClass().getSimpleName() + "\u001B[0m" + " and " + "\u001B[33;1m" + method.getDeclaringClass().getSimpleName() + "\u001B[0m");
                        buildStatus = false;
                    }else{
                        formulaList.put(uniqueId, method);
                    }
                    if (formulaNamesList.containsKey(name)) {
                        Method duplicateMethodInMap = formulaNamesList.get(name);
                        System.out.println();
                        System.out.println("\u001B[31m" + "\t--------------------------------DUPLICATE UNIQUE NAMES are found in FORMULAS--------------------------------" + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Unique name : " + "\u001B[0m" + "\u001B[33;1m" + name + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Methods     : " + "\u001B[0m" + "\u001B[33;1m" + duplicateMethodInMap.getName() + "\u001B[0m" + " and " + "\u001B[33;1m" + method.getName() + "\u001B[0m");
                        System.out.println("\u001B[36;1m" + "Classes     : " + "\u001B[0m" + "\u001B[33;1m" + duplicateMethodInMap.getDeclaringClass().getSimpleName() + "\u001B[0m" + " and " + "\u001B[33;1m" + method.getDeclaringClass().getSimpleName() + "\u001B[0m");
                        buildStatus = false;
                    }else{
                        formulaNamesList.put(name, method);
                    }

                    for(FormulaData formula:allFormulas){
                        if (name.equals(formula.getName())) {
                            if (!uniqueId.equals(formula.getUniqueId())) {
                                System.out.println();
                                System.out.println("\u001B[31m" + "\t--------------------------------DUPLICATE UNIQUE NAMES found for FORMULAS--------------------------------" + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Duplicated Name               : " + "\u001B[0m" + "\u001B[33;1m" + name + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Unique Name in method         : " + "\u001B[0m" + "\u001B[33;1m" + annotation.name() + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Unique Name in synced formula : " + "\u001B[0m" + "\u001B[33;1m" + formula.getName() + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Method                        : " + "\u001B[0m" + "\u001B[33;1m" + method.getName() + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Class                         : " + "\u001B[0m" + "\u001B[33;1m" + method.getDeclaringClass().getSimpleName() + "\u001B[0m");
                                buildStatus = false;
                            }
                        }if (uniqueId.equals(formula.getUniqueId())) {
                            if (!name.equals(formula.getName())) {
                                System.out.println();
                                System.out.println("\u001B[31m" + "\t--------------------------------DUPLICATE Unique ID's found for FORMULAS--------------------------------" + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Duplicated Unique Id          : " + "\u001B[0m" + "\u001B[33;1m" + annotation.uniqueId() + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Unique Name in method         : " + "\u001B[0m" + "\u001B[33;1m" + name + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Unique Name in synced formula : " + "\u001B[0m" + "\u001B[33;1m" + formula.getName() + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Method                        : " + "\u001B[0m" + "\u001B[33;1m" + method.getName() + "\u001B[0m");
                                System.out.println("\u001B[36;1m" + "Class                         : " + "\u001B[0m" + "\u001B[33;1m" + method.getDeclaringClass().getSimpleName() + "\u001B[0m");
                                buildStatus = false;
                            }
                        }
                    }
                }
            }
        }
    }
    private static void printSuccessMessage() {
        String successBanner = ANSI_GREEN +
                "\t\t\t\t                                                           \n" +
                "\t\t\t\t                                                           \n" +
                "\t\t\t\t  ███████╗██╗   ██╗ ██████╗ ██████╗███████╗███████╗███████╗\n" +
                "\t\t\t\t  ██╔════╝██║   ██║██╔════╝██╔════╝██╔════╝██╔════╝██╔════╝\n" +
                "\t\t\t\t  ███████╗██║   ██║██║     ██║     █████╗  ███████╗███████╗\n" +
                "\t\t\t\t  ╚════██║██║   ██║██║     ██║     ██╔══╝  ╚════██║╚════██║\n" +
                "\t\t\t\t  ███████║╚██████╔╝╚██████╗╚██████╗███████╗███████║███████║\n" +
                "\t\t\t\t  ╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝╚══════╝╚══════╝╚══════╝\n" +
                "\t\t\t\t                                                           \n" +
                "\t\t\t\t                      BUILD SUCCESS                        \n" +
                "\t\t\t\t             No duplicate Unique Id is found               \n" +
                "\t\t\t\t                                                           \n" + ANSI_RESET;

        System.out.println(successBanner);
    }

    private static void printErrorMessage() {
        String errorBanner = ANSI_RED +
                "\t\t\t\t                                             \n" +
                "\t\t\t\t                                             \n" +
                "\t\t\t\t  ███████╗ █████╗ ██╗██╗     ███████╗██████╗ \n" +
                "\t\t\t\t  ██╔════╝██╔══██╗██║██║     ██╔════╝██╔══██╗\n" +
                "\t\t\t\t  █████╗  ███████║██║██║     █████╗  ██║  ██║\n" +
                "\t\t\t\t  ██╔══╝  ██╔══██║██║██║     ██╔══╝  ██║  ██║\n" +
                "\t\t\t\t  ██║     ██║  ██║██║███████╗███████╗██████╔╝\n" +
                "\t\t\t\t  ╚═╝     ╚═╝  ╚═╝╚═╝╚══════╝╚══════╝╚═════╝ \n" +
                "\t\t\t\t                                             \n" +
                "\t\t\t\t                BUILD FAILED                 \n" +
                "\t\t\t\t                                             \n" + ANSI_RESET;

        System.out.println(errorBanner);
    }
}
