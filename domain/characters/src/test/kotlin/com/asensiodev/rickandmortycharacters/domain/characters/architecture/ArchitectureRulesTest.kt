package com.asensiodev.rickandmortycharacters.domain.characters.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.verify.assertTrue
import java.io.File
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArchitectureRulesTest {
    private lateinit var productionScope: KoScope

    @Before
    fun setUp() {
        val sourceRoots = checkNotNull(System.getProperty("architecture.sourceRoots"))
            .split(File.pathSeparator)
            .map(::File)
        sourceRoots.forEach { root ->
            assertTrue(
                "Missing production Kotlin sources: $root",
                root.isDirectory &&
                    root.walkTopDown().any { it.isFile && it.extension == "kt" },
            )
        }
        productionScope = Konsist.scopeFromExternalDirectories(sourceRoots.map { it.absolutePath })
    }

    @Test
    fun `GIVEN data implementation WHEN checking visibility THEN declarations stay encapsulated`() {
        val declarations = productionScope.classesAndInterfacesAndObjects(
            includeNested = false,
            includeLocal = false,
        )

        listOf("remote", "repository").forEach { owner ->
            val implementationDeclarations = declarations.filter {
                it.resideInPackage("com.asensiodev.rickandmortycharacters.data.characters.$owner..")
            }
            assertTrue("Missing data $owner declarations", implementationDeclarations.isNotEmpty())

            implementationDeclarations.assertTrue {
                it.hasInternalModifier || it.hasPrivateModifier
            }
        }
    }

    @Test
    fun `GIVEN ViewModels WHEN checking visibility THEN they remain internal`() {
        val viewModels = featureViewModels()

        viewModels.assertTrue { it.hasInternalModifier }
    }

    @Test
    fun `GIVEN screen state WHEN checking its contract THEN observation is explicitly read-only`() {
        val viewModels = featureViewModels()

        viewModels.forEach { viewModel ->
            val properties = viewModel.properties(includeNested = false)
            val stateProperties = properties.filter { it.name == "state" }
            assertTrue("Missing state in ${viewModel.name}", stateProperties.isNotEmpty())

            stateProperties.assertTrue {
                it.isVal && (it.hasPublicOrDefaultModifier || it.hasInternalModifier) &&
                    it.type?.hasSourceDeclarationOf(StateFlow::class) == true
            }
            properties.filterNot { it.hasPrivateModifier }.assertTrue {
                val type = it.type
                it.isVal && type != null &&
                    !type.hasSourceDeclarationOf(MutableStateFlow::class) &&
                    !type.hasSourceDeclarationOf(MutableSharedFlow::class)
            }
        }
    }

    private fun featureViewModels(): List<KoClassDeclaration> =
        listOf("home", "details").flatMap { feature ->
            val viewModels = productionScope.classes(includeNested = false, includeLocal = false)
                .filter {
                    it.resideInPackage(
                        "com.asensiodev.rickandmortycharacters.feature.$feature..",
                    ) &&
                        it.hasParentWithName("ViewModel")
                }
            assertTrue("Missing $feature ViewModel declarations", viewModels.isNotEmpty())
            viewModels
        }
}
