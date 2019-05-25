module io.github.subiyacryolite.jds {
    requires java.sql;
    requires kotlin.stdlib;
    requires com.fasterxml.jackson.annotation;
    requires javafx.base;
    requires kotlin.stdlib.jdk7;
    exports io.github.subiyacryolite.jds;
    exports io.github.subiyacryolite.jds.annotations;
    exports io.github.subiyacryolite.jds.beans.property;
    exports io.github.subiyacryolite.jds.embedded;
    exports io.github.subiyacryolite.jds.enums;
    exports io.github.subiyacryolite.jds.events;
    exports io.github.subiyacryolite.jds.utility;
}