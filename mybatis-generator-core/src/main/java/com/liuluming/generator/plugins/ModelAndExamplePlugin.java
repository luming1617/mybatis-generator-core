package com.liuluming.generator.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

public class ModelAndExamplePlugin extends PluginAdapter {

	ShellCallback shellCallback = null;

	public ModelAndExamplePlugin() {
		shellCallback = new DefaultShellCallback(false);
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {

		List<GeneratedJavaFile> javaFiles = new ArrayList<GeneratedJavaFile>();
		for (GeneratedJavaFile file : introspectedTable.getGeneratedJavaFiles()) {
			JavaFormatter javaFormatter = new DefaultJavaFormatter();

			CompilationUnit unit = file.getCompilationUnit();
			TopLevelClass topLevelClass = new TopLevelClass(changeType(unit.getType()));
			topLevelClass.setVisibility(JavaVisibility.PUBLIC);
			topLevelClass.addImportedType(unit.getType());
			topLevelClass.setSuperClass(unit.getType());
			GeneratedJavaFile javafile = new GeneratedJavaFile(topLevelClass, file.getTargetProject(), javaFormatter);

			try {
				File directory = shellCallback.getDirectory(javafile.getTargetProject(), javafile.getTargetPackage());

				File targetFile = new File(directory, javafile.getFileName());

				// 文件不存在
				if (!targetFile.exists()) {
					javaFiles.add(javafile);
				}
			} catch (ShellException e) {
				e.printStackTrace();
			}

		}

		return javaFiles;
	}

	private String changeType(FullyQualifiedJavaType fullyQualifiedJavaType) {
		String type = fullyQualifiedJavaType.getFullyQualifiedName();
		String temp = "base.Base";
		String newType = type.replace(temp, "");
		return newType;
	}
}
