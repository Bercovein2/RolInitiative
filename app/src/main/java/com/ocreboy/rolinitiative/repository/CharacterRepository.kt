package com.ocreboy.rolinitiative.repository

import com.ocreboy.rolinitiative.database.FolderDatabase
import com.ocreboy.rolinitiative.database.SavedCharacterDatabase
import com.ocreboy.rolinitiative.model.Folder
import com.ocreboy.rolinitiative.model.SavedCharacter

class CharacterRepository(
    private val characterDB: SavedCharacterDatabase,
    private val folderDB: FolderDatabase
) {

    suspend fun insert(item: SavedCharacter): Long = characterDB.getDao().insert(item)
    suspend fun update(item: SavedCharacter): Int = characterDB.getDao().update(item)
    suspend fun delete(item: SavedCharacter) = characterDB.getDao().delete(item)

    suspend fun insertFolder(item: Folder) = folderDB.getDao().insert(item)
    suspend fun updateFolder(item: Folder) = folderDB.getDao().update(item)


    fun getAll() = characterDB.getDao().getAll()

    fun search(query: String?) = characterDB.getDao().search(query)

    suspend fun getFoldersWithCharacters(): Map<Folder, List<SavedCharacter>> {
        val folders = folderDB.getDao().getAll() // Implementa getAllFolders() en tu DAO
        val characters = characterDB.getDao().getAll() // Implementa getAllCharacters() en tu DAO

        val foldersWithCharacters = folders.associateWith { folder ->
            characters.filter { it.folderId == folder.id }
        }

        val sortedFoldersWithCharacters = foldersWithCharacters.toSortedMap { folder1, folder2 ->
            folder1.name.compareTo(folder2.name, ignoreCase = true)
        }

        // Ordenar las listas de personajes dentro de cada carpeta
        val sortedFoldersWithSortedCharacters = sortedFoldersWithCharacters.mapValues { entry ->
            entry.value.sortedBy { it.name }
        }

        // Crear un mapa que asocie cada carpeta con su lista de personajes
        return sortedFoldersWithSortedCharacters
    }


    suspend fun deleteCharacterById(id: Int) {
        characterDB.getDao().deleteCharacterById(id)
    }

    suspend fun deleteCharactersByIds(ids: List<Int>) {
        characterDB.getDao().deleteCharactersByIds(ids)
    }
    suspend fun deleteFoldersByIds(ids: List<Int>) {
        folderDB.getDao().deleteFoldersByIds(ids)
    }
    suspend fun isFolderNameExists(name: String): Boolean {
        return folderDB.getDao().getFolderByName(name) != null
    }

}
