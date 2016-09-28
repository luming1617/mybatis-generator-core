package com.liuluming.generator.plugins;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;

/**
 * 
 * 定制部分mybatis的插件，主要实现以下功能
 * <ol>
 * <li>生成Model和Example的Base文件</li>
 * <li>生成新的sqlmap并覆盖xml文件</li>
 * <li>生成空的sqlmap custom的xml文件，不覆盖原来的，如果没有则创建空的</li>
 * </ol>
 * <p>
 * 使用方法配置与在generatorConfig.xml中其中</br>
 * baseCLassNamePrefix 为 新生成的类文件的前置关键字</br>
 * basePackage 为生成新的类文件的包名</br>
 * extXmlPackage 包名</br>
 * 
 * <plugin type="com.autostreets.mybatis.plugin.ModelAndExampleBaseClassPlugin">
 * <property name="baseCLassNamePrefix" value="Base"/>
 * <property name="basePackage" value="com.autostreets.model.base"/>
 * <property name="extXmlPackage" value="com.autostreets.model.base.ext"/>
 * </plugin>
 * 
 * 
 *
 * @author Chenshi
 *
 **/
public class ModelAndExampleBaseClassPlugin extends PluginAdapter {

	/**
	 * 类文件的前缀名称
	 */
	private String baseCLassNamePrefix;

	/**
	 * 类文件包名
	 */
	private String basePackage;

	/**
	 * 空的xml文件生成包
	 */
	private String extXmlPackage;

	/**
	 * 利用java反射获取isMergeable参数，并修改
	 */
	private Field isMergeableFid = null;

	/**
	 * 两个参数用于做数据中转
	 */
	private String oldTypeName;
	private String oldExampleName;

	public ModelAndExampleBaseClassPlugin() {
		try {
			if (isMergeableFid == null) {
				isMergeableFid = GeneratedXmlFile.class.getDeclaredField("isMergeable");
				isMergeableFid.setAccessible(true);
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		// 初始化两参数为空
		oldTypeName = null;
		oldExampleName = null;

		oldExampleName = introspectedTable.getExampleType();
		introspectedTable.setExampleType(changeTypeName(oldExampleName));

		oldTypeName = introspectedTable.getBaseRecordType();
		introspectedTable.setBaseRecordType(changeTypeName(oldTypeName));

	}

	/*
	 * 检查xml参数是否正确
	 * 
	 * @see org.mybatis.generator.api.Plugin#validate(java.util.List)
	 */
	@Override
	public boolean validate(List<String> warnings) {

		baseCLassNamePrefix = properties.getProperty("baseCLassNamePrefix");

		boolean valid = stringHasValue(baseCLassNamePrefix);

		basePackage = properties.getProperty("basePackage");
		boolean valid2 = stringHasValue(basePackage);

		extXmlPackage = properties.getProperty("extXmlPackage");
		boolean valid3 = stringHasValue(extXmlPackage);

		return valid && valid2 && valid3;
	}

	/*
	 * 生成新的xml文件 ,覆盖原来存在文件
	 * 
	 * @see
	 * org.mybatis.generator.api.PluginAdapter#contextGenerateAdditionalXmlFiles
	 * (org.mybatis.generator.api.IntrospectedTable)
	 */
	@Override
	public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {
		System.out.println("===============开始生成xml文件================");

		if (oldTypeName != null) {
			introspectedTable.setBaseRecordType(oldTypeName);
		}

		if (oldExampleName != null) {
			introspectedTable.setExampleType(oldExampleName);
		}

		List<GeneratedXmlFile> answer = new ArrayList<GeneratedXmlFile>(1);
		List<GeneratedXmlFile> xmlFiles = introspectedTable.getGeneratedXmlFiles();

		Document document = new Document(XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID,
				XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);
		XmlElement root = new XmlElement("mapper");
		document.setRootElement(root);

		for (GeneratedXmlFile xmlFile : xmlFiles) {
			try {
				// 将xml的isMergeabl改为false
				isMergeableFid.set(xmlFile, false);

				// 生成新的空的xml 但是不覆盖
				root.addAttribute(new Attribute("namespace", introspectedTable.getMyBatis3FallbackSqlMapNamespace()));

				GeneratedXmlFile gxf = new GeneratedXmlFile(document, xmlFile.getFileName(), extXmlPackage,
						xmlFile.getTargetProject(), true, context.getXmlFormatter());

				answer.add(gxf);

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			answer.add(xmlFile);
		}

		System.out.println("===============完成生成xml文件================");

		return answer;
	}

	/**
	 * 生成Base文件名
	 * 
	 * @param oldType
	 * @return 新的名称
	 */
	private String changeTypeName(String oldType) {

		int lastDonlocation = oldType.lastIndexOf('.');
		return basePackage + "." + baseCLassNamePrefix + oldType.substring(lastDonlocation + 1).substring(1);

	}

}
