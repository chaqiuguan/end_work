<template>
  <div class="skeleton-grid">
    <div v-for="i in count" :key="i" class="skeleton-card">
      <div class="skeleton skeleton-image"></div>
      <div class="skeleton skeleton-title"></div>
      <div class="skeleton skeleton-price"></div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  /** 骨架屏数量 */
  count: {
    type: Number,
    default: 8
  }
})
</script>

<style scoped>
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.skeleton-card {
  background: #fff;
  border-radius: var(--radius);
  padding: 0 0 16px 0;
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  padding-top: 100%;
}

.skeleton-title {
  height: 20px;
  margin: 12px 12px 8px;
}

.skeleton-price {
  height: 24px;
  width: 40%;
  margin: 0 12px;
}

@media (max-width: 768px) {
  .skeleton-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }
}
</style>
