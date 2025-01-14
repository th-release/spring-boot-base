package com.threlease.base.common.configs;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * JPA 데이터베이스 테이블과 컬럼 명을 매핑할 때 기본적으로 자바의 네이밍 규칙을 따르거나 사용자가 지정한 규칙을 따를 수 있습니다.
 * 이 클래스는 Hibernate의 네이밍 전략을 사용자 정의하기 위해 사용됩니다.
 */
public class CustomPhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return applyQuoting(name); // 테이블 이름에 큰따옴표 적용
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return applyQuoting(name); // 컬럼 이름에 큰따옴표 적용
    }

    private Identifier applyQuoting(Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        // 이미 큰따옴표로 감싸지지 않은 경우 큰따옴표 적용
        String quotedText = "\"" + identifier.getText() + "\"";
        return Identifier.toIdentifier(quotedText, identifier.isQuoted());
    }
}

