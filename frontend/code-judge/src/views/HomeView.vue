<template>
  <div class="home">
    <h1 class="title">欢迎</h1>
    <p class="lead">选择「题库」浏览题目并提交代码。</p>
    <el-button type="primary" class="cta" @click="$router.push({ name: 'problems' })">
      进入题库
    </el-button>
  </div>
</template>

<script setup></script>

<style scoped>
.home {
  padding-top: 2rem;
}

.title {
  margin: 0 0 0.75rem;
  font-size: 1.5rem;
  font-weight: 600;
}

.lead {
  margin: 0 0 2rem;
  font-size: 0.9375rem;
  color: #404040;
  line-height: 1.6;
}

.cta {
  border-radius: 0;
  --el-button-bg-color: #000000;
  --el-button-border-color: #000000;
  --el-button-hover-bg-color: #333333;
  --el-button-hover-border-color: #333333;
}
</style>
