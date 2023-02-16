module voidframework.core {

    // Java
    requires java.base;
    requires java.management;
    requires java.xml;

    // Dependencies
    requires com.esotericsoftware.kryo;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.fasterxml.jackson.datatype.jdk8;
    requires com.fasterxml.jackson.datatype.joda;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.google.guice;
    requires io.github.classgraph;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires typesafe.config;

    // Components
    exports dev.voidframework.core.bindable;
    exports dev.voidframework.core.classestoload.generator;
    exports dev.voidframework.core.classestoload;
    exports dev.voidframework.core.conditionalfeature.condition;
    exports dev.voidframework.core.conditionalfeature;
    exports dev.voidframework.core.constant;
    exports dev.voidframework.core.conversion.impl;
    exports dev.voidframework.core.conversion;
    exports dev.voidframework.core.lang;
    exports dev.voidframework.core.lifecycle;
    exports dev.voidframework.core.module;
    exports dev.voidframework.core.remoteconfiguration;
    exports dev.voidframework.core.utils;
    exports dev.voidframework.core;
}
