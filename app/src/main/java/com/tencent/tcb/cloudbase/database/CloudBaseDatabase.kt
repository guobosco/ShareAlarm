package com.tencent.tcb.cloudbase.database

import com.tencent.tcb.cloudbase.CloudBaseCore

class CloudBaseDatabase {
    private val collections = mutableMapOf<String, CloudBaseCollection>()
    
    fun collection(collectionName: String): CloudBaseCollection {
        return collections.getOrPut(collectionName) { CloudBaseCollection(collectionName) }
    }
}

class CloudBaseCollection(private val collectionName: String) {
    private val documents = mutableMapOf<String, Any>()
    
    fun doc(docId: String): CloudBaseDocument {
        return CloudBaseDocument(collectionName, docId, documents)
    }

    fun add(data: Any): CloudBaseDocumentReference {
        // Generate unique document ID
        val docId = "doc_${System.currentTimeMillis()}"
        // Store document in memory
        documents[docId] = data
        println("Added document to $collectionName with id: $docId")
        return CloudBaseDocumentReference(collectionName, docId, documents)
    }

    fun whereEqualTo(field: String, value: Any): CloudBaseQuery {
        return CloudBaseQuery(collectionName, documents).whereEqualTo(field, value)
    }

    fun whereArrayContains(field: String, value: Any): CloudBaseQuery {
        return CloudBaseQuery(collectionName, documents).whereArrayContains(field, value)
    }

    fun get(): CloudBaseQueryResult {
        // Return all documents
        val results = documents.map { (id, data) -> 
            val dataMap = data as? Map<String, Any> ?: emptyMap()
            CloudBaseDocumentSnapshot(id, dataMap)
        }
        println("Retrieved ${results.size} documents from $collectionName")
        return CloudBaseQueryResult(results)
    }
    
    fun getDocument(docId: String): Any? {
        return documents[docId]
    }
    
    fun setDocument(docId: String, data: Any) {
        documents[docId] = data
        println("Set document $docId in $collectionName")
    }
    
    fun updateDocument(docId: String, data: Any) {
        val existingData = documents[docId]
        if (existingData != null) {
            // Simple update: replace with new data
            documents[docId] = data
            println("Updated document $docId in $collectionName")
        }
    }
    
    fun deleteDocument(docId: String) {
        documents.remove(docId)
        println("Deleted document $docId from $collectionName")
    }
}

class CloudBaseDocument(
    private val collectionName: String, 
    private val docId: String, 
    private val documents: MutableMap<String, Any>
) {
    fun get(): CloudBaseResult {
        val data = documents[docId] as? Map<String, Any> ?: emptyMap()
        println("Retrieved document $docId from $collectionName: $data")
        return CloudBaseResult(mapOf(
            "_id" to docId,
            "data" to data
        ))
    }

    fun set(data: Any): Unit {
        documents[docId] = data
        println("Set document $docId in $collectionName: $data")
    }

    fun update(data: Any): Unit {
        val existingData = documents[docId]
        if (existingData != null) {
            documents[docId] = data
            println("Updated document $docId in $collectionName: $data")
        }
    }

    fun delete(): Unit {
        documents.remove(docId)
        println("Deleted document $docId from $collectionName")
    }
}

class CloudBaseDocumentReference(
    private val collectionName: String, 
    val id: String, 
    private val documents: MutableMap<String, Any>
) {
    fun get(): CloudBaseResult {
        val data = documents[id] as? Map<String, Any> ?: emptyMap()
        return CloudBaseResult(mapOf(
            "_id" to id,
            "data" to data
        ))
    }
}

class CloudBaseQuery(
    private val collectionName: String, 
    private val documents: MutableMap<String, Any>
) {
    private val equalToFilters = mutableMapOf<String, Any>()
    private val arrayContainsFilters = mutableMapOf<String, Any>()

    fun whereEqualTo(field: String, value: Any): CloudBaseQuery {
        equalToFilters[field] = value
        return this
    }

    fun whereArrayContains(field: String, value: Any): CloudBaseQuery {
        arrayContainsFilters[field] = value
        return this
    }

    fun get(): CloudBaseQueryResult {
        val results = documents.filter { (id, data) ->
            // Apply equalTo filters
            val equalToMatch = equalToFilters.all { (field, value) ->
                val dataMap = data as? Map<*, *>
                dataMap?.get(field) == value
            }
            
            // Apply arrayContains filters
            val arrayContainsMatch = arrayContainsFilters.all { (field, value) ->
                val dataMap = data as? Map<*, *>
                val arrayValue = dataMap?.get(field) as? List<*>
                arrayValue?.contains(value) ?: false
            }
            
            equalToMatch && arrayContainsMatch
        }.map { (id, data) ->
            val dataMap = data as? Map<String, Any> ?: emptyMap()
            CloudBaseDocumentSnapshot(id, dataMap)
        }
        
        println("Query $collectionName returned ${results.size} documents")
        return CloudBaseQueryResult(results)
    }
}

class CloudBaseResult(private val rawData: Map<String, Any>) {
    val data: Map<String, Any>?
        get() = rawData["data"] as? Map<String, Any>
}

class CloudBaseQueryResult(private val rawData: List<CloudBaseDocumentSnapshot>) {
    val data: List<CloudBaseDocumentSnapshot>
        get() = rawData
}

class CloudBaseDocumentSnapshot(private val docId: String, private val rawData: Map<String, Any>) {
    val id: String
        get() = docId

    val data: Map<String, Any>
        get() = rawData
}
