package com.moviebooking.bookingservice.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * ArchUnit — Architecture Validation Tests
 * Ensures proper layering: Controller → Service → Repository
 */
@AnalyzeClasses(
        packages = "com.moviebooking.bookingservice",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule layer_dependencies_are_respected =
            layeredArchitecture().consideringOnlyDependenciesInLayers()
                    .layer("Controller").definedBy("..controller..")
                    .layer("Service").definedBy("..service..")
                    .layer("Repository").definedBy("..repository..")
                    .layer("Entity").definedBy("..entity..")
                    .layer("DTO").definedBy("..dto..")
                    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
                    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses().that().resideInAPackage("..service.impl..")
                    .should().dependOnClassesThat().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule repositories_should_not_depend_on_services =
            noClasses().that().resideInAPackage("..repository..")
                    .should().dependOnClassesThat().resideInAPackage("..service..");

    @ArchTest
    static final ArchRule controllers_should_be_suffixed =
            classes().that().resideInAPackage("..controller..")
                    .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule services_should_be_interfaces_or_impl =
            classes().that().resideInAPackage("..service.impl..")
                    .should().haveSimpleNameEndingWith("Impl");

    @ArchTest
    static final ArchRule repositories_should_be_interfaces =
            classes().that().resideInAPackage("..repository..")
                    .should().beInterfaces();

    @ArchTest
    static final ArchRule entities_should_reside_in_entity_package =
            classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
                    .should().resideInAPackage("..entity..");
}
