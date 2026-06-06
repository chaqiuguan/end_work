<template>
  <div class="pagination-wrapper" v-if="total > 0">
    <el-pagination
      v-model:current-page="current"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[12, 20, 40]"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="emitChange"
      @current-change="emitChange"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 20 }
})

const emit = defineEmits(['change'])

const current = ref(props.page)
const pageSize = ref(props.size)

watch(() => props.page, (val) => { current.value = val })
watch(() => props.size, (val) => { pageSize.value = val })

function emitChange() {
  emit('change', { page: current.value, size: pageSize.value })
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px 0;
}
</style>
