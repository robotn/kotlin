/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.plugin

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunction
import org.jetbrains.kotlin.fir.extensions.FirExistingClassModificationExtension
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.and
import org.jetbrains.kotlin.fir.extensions.predicate.has
import org.jetbrains.kotlin.fir.extensions.predicate.under
import org.jetbrains.kotlin.fir.resolve.transformers.plugin.GeneratedNestedClass
import org.jetbrains.kotlin.fir.symbols.CallableId
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class AllOpenMemberGenerator(session: FirSession) : FirExistingClassModificationExtension(session) {
    override fun generateNestedClasses(
        annotatedDeclaration: FirDeclaration,
        owners: List<FirAnnotatedDeclaration>
    ): List<GeneratedDeclaration<FirRegularClass>> {
        return emptyList()
    }

    override fun generateMembersForNestedClasses(generatedNestedClass: GeneratedNestedClass): List<FirDeclaration> {
        return emptyList()
    }

    override fun generateMembers(
        annotatedDeclaration: FirDeclaration,
        owners: List<FirAnnotatedDeclaration>
    ): List<GeneratedDeclaration<*>> {
        if (annotatedDeclaration !is FirProperty) return emptyList()
        val owner = owners.last() as? FirRegularClass ?: return emptyList()
        val propertyName = annotatedDeclaration.name.identifier
        val function = buildSimpleFunction {
            session = this@AllOpenMemberGenerator.session
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            origin = FirDeclarationOrigin.Plugin(key)
            returnTypeRef = annotatedDeclaration.returnTypeRef
            status = annotatedDeclaration.status
            name = Name.identifier("hello${propertyName.capitalize()}")
            symbol = FirNamedFunctionSymbol(CallableId(owner.classId, name), isFakeOverride = false)
        }
        return listOf(GeneratedDeclaration(function, owner))
    }

    override val predicate: DeclarationPredicate = under("WithGenerated".fqn()) and has("WithHello".fqn())

    override val key: FirPluginKey
        get() = AllOpenPluginKey
}

private fun String.fqn(): FqName = FqName("org.jetbrains.kotlin.fir.plugin.$this")