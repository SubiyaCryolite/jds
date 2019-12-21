/**
 * Jenesis Data Store Copyright (c) 2017 Ifunga Ndana. All rights reserved.
 *
 * 1. Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 2. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 3. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name Jenesis Data Store nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.subiyacryolite.jds.tests

import io.github.subiyacryolite.jds.tests.common.BaseTestConfig
import io.github.subiyacryolite.jds.tests.common.TestData
import io.github.subiyacryolite.jds.tests.entities.Example
import io.github.subiyacryolite.jds.context.DbContext
import io.github.subiyacryolite.jds.Load
import io.github.subiyacryolite.jds.Save
import io.github.subiyacryolite.jds.enums.FilterBy
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class LoadAndSaveTests : BaseTestConfig("Load and Save tests") {

    @Throws(Exception::class)
    override fun testImpl(dbContext: DbContext) {
        save(dbContext)
        load(dbContext)
    }

    @Throws(Exception::class)
    private fun save(dbContext: DbContext) {
        val save = Save(dbContext, TestData.collection)
        val process = Executors.newSingleThreadExecutor().submit(save)
        while (!process.isDone)
            Thread.sleep(16)
        System.out.printf("Saved? %s\n", process.get())
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    private fun load(dbContext: DbContext) {
        val loadAllInstances = Load(dbContext, Example::class.java)
        val loadSpecificInstance = Load(dbContext, Example::class.java, FilterBy.Id, setOf("instance3"))
        val loadSortedInstances = Load(dbContext, Example::class.java)

        val executorService = Executors.newFixedThreadPool(3)
        val loadingAllInstances = executorService.submit(loadAllInstances)
        val loadingSpecificInstance = executorService.submit(loadSpecificInstance)
        val loadingSortedInstances = executorService.submit(loadSortedInstances)

        while (!loadingAllInstances.isDone)
            Thread.sleep(16)
        while (!loadingSpecificInstance.isDone)
            Thread.sleep(16)
        while (!loadingSortedInstances.isDone)
            Thread.sleep(16)

        val allInstances = loadingAllInstances.get()
        val specificInstance = loadingSpecificInstance.get()
        val sortedInstances = loadingSortedInstances.get()

        println(allInstances)
        println(specificInstance)
        println(sortedInstances)

        println("DONE")
    }
}
