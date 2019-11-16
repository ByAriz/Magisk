package com.topjohnwu.magisk.model.entity.recycler

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.topjohnwu.magisk.BR
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.databinding.ComparableRvItem
import com.topjohnwu.magisk.extensions.addOnPropertyChangedCallback
import com.topjohnwu.magisk.extensions.get
import com.topjohnwu.magisk.extensions.toggle
import com.topjohnwu.magisk.model.entity.module.Module
import com.topjohnwu.magisk.model.entity.module.Repo
import com.topjohnwu.magisk.redesign.module.ModuleViewModel
import com.topjohnwu.magisk.utils.KObservableField

class ModuleRvItem(val item: Module) : ComparableRvItem<ModuleRvItem>() {

    override val layoutRes: Int = R.layout.item_module

    val lastActionNotice = KObservableField("")
    val isChecked = KObservableField(item.enable)
    val isDeletable = KObservableField(item.remove)

    init {
        isChecked.addOnPropertyChangedCallback {
            when (it) {
                true -> {
                    item.enable = true
                    notice(R.string.disable_file_removed)
                }
                false -> {
                    item.enable = false
                    notice(R.string.disable_file_created)
                }
            }
        }
        isDeletable.addOnPropertyChangedCallback {
            when (it) {
                true -> {
                    item.remove = true
                    notice(R.string.remove_file_created)
                }
                false -> {
                    item.remove = false
                    notice(R.string.remove_file_deleted)
                }
            }
        }
        when {
            item.updated -> notice(R.string.update_file_created)
            item.remove -> notice(R.string.remove_file_created)
        }
    }

    fun toggle() = isChecked.toggle()
    fun toggleDelete() = isDeletable.toggle()

    private fun notice(@StringRes info: Int) {
        lastActionNotice.value = get<Resources>().getString(info)
    }

    override fun contentSameAs(other: ModuleRvItem): Boolean = item.version == other.item.version
            && item.versionCode == other.item.versionCode
            && item.description == other.item.description
            && item.name == other.item.name

    override fun itemSameAs(other: ModuleRvItem): Boolean = item.id == other.item.id
}

class RepoRvItem(val item: Repo) : ComparableRvItem<RepoRvItem>() {

    override val layoutRes: Int = R.layout.item_repo

    override fun contentSameAs(other: RepoRvItem): Boolean = item == other.item

    override fun itemSameAs(other: RepoRvItem): Boolean = item.id == other.item.id
}

object InstallModule : ComparableRvItem<InstallModule>() {
    override val layoutRes = R.layout.item_module_download

    override fun contentSameAs(other: InstallModule) = this == other
    override fun itemSameAs(other: InstallModule) = this === other
}

class SectionTitle(
    val title: Int,
    val button: Int = 0,
    val icon: Int = 0
) : ComparableRvItem<SectionTitle>() {
    override val layoutRes = R.layout.item_section_md2

    val hasButton = KObservableField(button != 0 || icon != 0)

    override fun onBindingBound(binding: ViewDataBinding) {
        super.onBindingBound(binding)
        val params = binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams
        params.isFullSpan = true
    }

    override fun itemSameAs(other: SectionTitle): Boolean = this === other
    override fun contentSameAs(other: SectionTitle): Boolean = this === other
}

class RepoItem(val item: Repo) : ComparableRvItem<RepoItem>() {

    override val layoutRes: Int = R.layout.item_repo_md2

    val progress = KObservableField(0)

    override fun contentSameAs(other: RepoItem): Boolean = item == other.item
    override fun itemSameAs(other: RepoItem): Boolean = item.id == other.item.id
}

class ModuleItem(val item: Module) : ObservableItem<ModuleItem>(), Observable {

    override val layoutRes = R.layout.item_module_md2

    @get:Bindable
    var isEnabled = item.enable
        set(value) {
            field = value
            item.enable = value
            notifyChange(BR.enabled)
        }
    @get:Bindable
    var isRemoved = item.remove
        set(value) {
            field = value
            item.remove = value
            notifyChange(BR.removed)
        }

    val isUpdated get() = item.updated
    val isModified get() = isRemoved || item.updated

    fun toggle() {
        isEnabled = !isEnabled
    }

    fun delete(viewModel: ModuleViewModel) {
        isRemoved = !isRemoved
        viewModel.moveToState()
    }

    override fun contentSameAs(other: ModuleItem): Boolean = item.version == other.item.version
            && item.versionCode == other.item.versionCode
            && item.description == other.item.description
            && item.name == other.item.name

    override fun itemSameAs(other: ModuleItem): Boolean = item.id == other.item.id

}

abstract class ObservableItem<T> : ComparableRvItem<T>(), Observable {

    private val list = PropertyChangeRegistry()

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        list.remove(callback ?: return)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        list.add(callback ?: return)
    }

    protected fun notifyChange(id: Int) = list.notifyChange(this, id)

}