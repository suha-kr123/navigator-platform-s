plugins {
    id("java-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
    id("common-feature-conventions")
}


dependencies {
    implementation(project(":common"))
    implementation(project(":features:creditbureau"))
    implementation(project(":features:consent"))
    implementation(project(":features:person"))
    implementation(project(":features:usermanagement"))
    implementation(project(":features:advisor"))
    implementation(project(":features:master"))
    implementation(project(":features:address"))
    implementation(project(":features:lead"))
    implementation(project(":features:lender"))
    implementation(project(":features:document"))
    implementation(project(":features:offices"))
    implementation(project(":features:notes"))
    implementation(project(":features:notifications"))
    implementation(project(":features:marketing"))
    implementation(project(":features:workflow"))
    implementation(project(":features:stage"))
    implementation(project(":features:task"))
    implementation(project(":features:staff"))
    implementation(project(":features:identifier"))
    implementation(project(":features:campaign"))
    implementation(project(":redash"))
    implementation(project(":features:bulk-operations"))
    implementation(project(":externals:exotel"))
    implementation(project(":externals:whatsapp"))
    implementation(project(":externals:creditbureau"))
    implementation(project(":externals:customer"))
    implementation(project(":externals:masters"))
    implementation(project(":features:referral"))
    implementation(project(":features:bre"))
    implementation(project(":features:atlas"))
    implementation(project(":externals:atlas"))
    implementation(project(":analytics"))
}

springBoot {
    mainClass.set("com.nivasafinance.NavigatorApplication")
}
