package com.tianli.generator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * Mysql代码生成器
 * </p>
 *
 * @author Caratacus
 */
public class MysqlGenerator {
    /**
     * 作者
     */
    private static final String AUTHOR = "xianeng";
    /**
     * 需生成数据库表
     */
    private static final String[] TABLE_ARR = new String[]{"financial_pledge_info"};

    public static void main(String[] args) {
        generator();
    }

    /**
     * <p>
     * MySQL generator
     * </p>
     */
    public static void generator() {

        String projectPath = System.getProperty("user.dir");

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();

        // 输出目录
        gc.setOutputDir(projectPath + "/src/main/java");
        // 是否覆盖文件
        gc.setFileOverride(true);
        // 开启 activeRecord 模式
        gc.setActiveRecord(true);
        // XML 二级缓存
        gc.setEnableCache(false);
        // XML ResultMap
        gc.setBaseResultMap(false);
        // XML columList
        gc.setBaseColumnList(false);
        // 是否生成 kotlin 代码
        gc.setKotlin(false);
        // 作者
        gc.setAuthor(AUTHOR);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        // 数据库类型
        dsc.setDbType(DbType.MYSQL);
        dsc.setTypeConvert(new MySqlTypeConvert() {
            // 数据库字段类型与java实体类属性类型映射
            @Override
            public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                if ("bit".equals(fieldType.toLowerCase())) {
                    return DbColumnType.BOOLEAN;
                }
                if ("tinyint".equals(fieldType.toLowerCase())) {
                    return DbColumnType.BOOLEAN;
                }
                if ("date".equals(fieldType.toLowerCase())) {
                    return DbColumnType.LOCAL_DATE;
                }
                if ("time".equals(fieldType.toLowerCase())) {
                    return DbColumnType.LOCAL_TIME;
                }
                if ("datetime".equals(fieldType.toLowerCase())) {
                    return DbColumnType.LOCAL_DATE_TIME;
                }
                return super.processTypeConvert(globalConfig, fieldType);
            }
        });
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("financial");
        dsc.setPassword("@tianli123456TL");
        dsc.setUrl("jdbc:mysql://rm-6we8u0l05o7z6tf76jo.mysql.japan.rds.aliyuncs.com:3306/financial?characterEncoding=UTF-8");
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.tianli.borrow");
        pc.setController("controller");
        pc.setEntity("entity");
        pc.setMapper("dao");
        pc.setService("service");
        pc.setServiceImpl("service.impl");

        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        // 如果模板引擎是 velocity
        String templatePath = "/templates/mapper.xml.vm";
        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);
        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        // 全局大写命名
        strategy.setCapitalMode(false);
        // 表名生成策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);
        // 需要生成的表
        strategy.setInclude(TABLE_ARR);
        strategy.setRestControllerStyle(true);
        mpg.setStrategy(strategy);

        mpg.execute();
    }
}
