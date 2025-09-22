plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}


dependencies {
    implementation(project(":common"))
    implementation(project(":features:creditbureau"))
    implementation(project(":features:person"))
    implementation(project(":features:advisor"))
    implementation(project(":features:advisorleadmapping"))
    implementation(project(":features:master"))
    implementation(project(":features:address"))
    implementation(project(":features:lead"))
    implementation(project(":features:document"))
    implementation(project(":features:identifiers"))
    implementation(project(":features:income"))
    implementation(project(":features:notes"))
}
