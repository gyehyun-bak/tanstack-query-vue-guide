## Abstraction

해당 문서는 *KB IT’s Your Life*의 19반 2팀(계모임)에서 `Vue` 프론트엔드 개발 시 사용할 `TanStack Query` 라이브러리에 대해 통일성 있는 개발 컨벤션을 위한 가이드를 제공하기 위해 작성되었습니다.

`TanStack Query` 라이브러리에 대한 간략한 소개와 Vue에서 `useQuery`와 `useMutation`을 사용하는 방법을 예시와 함께 설명합니다.

해당 문서는 `TanStack Query` [공식 문서](https://tanstack.com/query/latest/docs/framework/vue/overview)를 참고하여 작성되었습니다. 해당 코드는 `TypeScript`로 작성되었으며, `axios` 라이브러리를 사용합니다.

완성된 소스 코드를 [깃허브](https://github.com/gyehyun-bak/tanstack-query-vue-guide)에서 참조하실 수 있습니다.

- `Vue` 코드는 `client` 디렉토리에 포함되어 있습니다.
- 실제 동작 테스트를 위해 함께 사용할 수 있는 `Spring Boot` 서버가 `server` 디렉토리에 포함되어 있습니다.

## Overview

`TanStack Query`는 기존 프론트엔드 개발에서 데이터 페칭(data-fetching)과 관련된 로직을 다룰 때 직접 구현해야 했던 다양한 상세 기능들을, 통일된 API로 제공하는 라이브러리입니다.

데이터를 단순히 가져오는 작업부터 캐싱, 동기화, 업데이트 등 여러 가지 다양한 서버와의 상호작용 관련 상황에서 사용할 수 있는 미리 만들어진 편리한 기능들을 제공합니다.

많은 기능이 있지만 개발 초기 단계에는 다음과 같은 기능들이 유용할 수 있습니다:

- 데이터 조회(useQuery)
- 데이터 업데이트(useMutation)
- 로딩 상태(isLoading, isPending)
- 성공 처리(isSuccess, onSuccess)
- 에러 처리(isError, onError)
- 새로 고침(refetch)
- 자동 재시도(retry)
- 페이지네이션(Pagination)
- 무한 스크롤(Infinite Queries)

모두 API 처리 관련해서 매번 등장하는 공통 관심사입니다. 이 모든 기능을 `try-catch`로 처리하거나 개별 커스텀 훅을 만드는 것은 아주 번거로운 작업이 될 수 있습니다. `TanStack Query`는 이들을 간편하게 해결해주는 API를 제공합니다. 해당 문서에서는 이 중 몇 가지를 살펴볼 것입니다.

## Installation

NPM을 통해 Vue Query(TanStack Query)를 설치할 수 있습니다.

```bash
npm i @tanstack/vue-query
```

라이브러리가 설치되면 `VueQueryPlugin`의 초기화가 필요합니다. 다음과 같이 `main.ts` 파일을 수정합니다.

```ts
import { createApp } from 'vue'
import App from './App.vue'

import { VueQueryPlugin } from '@tanstack/vue-query'

createApp(App).use(VueQueryPlugin).mount('#app')
```

Vue의 `<script setup>`과 Composition API를 이용합니다. 아래는 사용 코드의 예시입니다.

```vue
<script setup>
import { useQuery } from '@tanstack/vue-query'

const { isPending, isFetching, isError, data, error } = useQuery({
  queryKey: ['todos'],
  queryFn: getTodos,
})
</script>

<template>...</template>
```

## UseQuery

`useQuery`는 일반적인 데이터 페칭(data-fetching)에 사용됩니다. 호출하는 API 별로 원하는 유니크 키(unique key)를 설정하여 사용합니다. 실제 API를 호출하는 함수를 `queryFn`으로 전달합니다. `queryFn`은 Promise를 반환하는 어떤 함수도 가능합니다. 주로 `axios`를 이용한 API 호출 함수를 매개변수로 전달합니다.

```ts
import { useQuery } from '@tanstack/vue-query'

const result = useQuery({ queryKey: ['todos'], queryFn: getTodos })
```

`useQuery`를 통해 반환되는 객체에는 다양한 프로퍼티가 포함되어 있습니다. 이들을 구조 분해 할당으로 가져올 수 있습니다.

```ts
import { useQuery } from '@tanstack/vue-query'

const { isPending, isError, data, error } = useQuery({
  queryKey: ['todos'],
  queryFn: getTodos,
})
```

Todo 리스트 같은 단순한 서비스를 예시로 들 수 있습니다. 다음과 같이 API 호출 함수가 정의되어 있습니다.

- `getTodos.ts`

```ts
import axios from 'axios'

export const getTodos = async () => {
  return (await axios.get('/api/todos')).data
}
```

원하는 데이터를 비동기로 서버에 요청했을 때, 로딩, 에러 등에 따라 다른 UI를 표시하고 싶은 경우 다음과 같이 사용할 수 있습니다.

- `App.vue`

```vue
<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { getTodos } from './api/getTodos'

const { data, isPending, isError, error } = useQuery({
  queryKey: ['todos'],
  queryFn: getTodos,
})
</script>

<template>
  <span v-if="isPending">Loading...</span>
  <span v-else-if="isError">Error: {{ error?.message }}</span>
  <ul v-else-if="data">
    <li v-for="todo in data" :key="todo.id">{{ todo.title }}</li>
  </ul>
</template>
```

`useQuery`는 컴포넌트가 마운트될 때 자동으로 호출됩니다. `useQuery`를 통해 `isPending`, `isError`, `data`, `error`를 받아옵니다. 각 데이터는 현재 API가 요청 중인지, 성공했는지의 여부에 따라 자동으로 업데이트됩니다.

- `isPending`은 데이터를 아직 불러오기 전인지 표현합니다. 이를 활용하여 로딩 화면을 구성할 수 있습니다.
- `isError`는 비동기 함수가 에러를 반환했는지 여부를 표현합니다. 이를 활용하여 요청 에러 시 로직을 구성할 수 있습니다.
- `error`에는 에러가 반환된 경우 에러 객체가 할당됩니다. null 체크가 필요합니다.
- `data`는 `axios` 함수가 성공적으로 반환한 데이터입니다. null 체크가 필요합니다.

추가적인 내용은 [여기](https://tanstack.com/query/latest/docs/framework/vue/guides/queries)를 참고하세요.

## Refetch

특정 API는 정해진 시간마다, 혹은 버튼을 누를 때 다시 호출하고 싶은 경우가 있습니다. `useQuery`의 `refetch`와 `refetchInterval` 옵션 등을 통해 구현할 수 있습니다.

- `RefetchExample.vue`

```vue
<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { getTodos } from './api/getTodos'

const { data, isPending, isError, error, refetch } = useQuery({
  queryKey: ['todos'],
  queryFn: getTodos,
  refetchInterval: 5000, // 5초마다 자동 새로고침
})
</script>

<template>
  <button @click="() => refetch()">새로고침</button>

  <div v-if="isPending">로딩 중...</div>
  <div v-else-if="isError">에러: {{ error?.message }}</div>
  <ul v-else>
    <li v-for="todo in data" :key="todo.id">{{ todo.title }}</li>
  </ul>
</template>
```

## UseMutation

`useMutation`은 데이터의 생성/수정/삭제에 주로 사용됩니다. `useMutation`은 `mutationFn`으로 전달된 비동기 함수를 원하는 때에 호출할 수 있도록 `mutate`라는 함수를 제공합니다. 또한 `useQuery`와 마찬가지로 `data`, `isPending`, `isSuccess`, `isError`, `error`를 지원합니다.

`useQuery`와의 차이점은 1) `mutate` 호출 전까지 호출되지 않는다는 점과 2) 호출 이후 응답의 성공 여부에 따라 콜백 함수를 지정할 수 있다는 점이 있습니다. 

`onSuccess`와 `onError`, `onSettled`등의 속성에 콜백 함수를 제공하면, 하나의 함수 호출 안에서 요청 성공/실패 시 다음 동작을 간편하게 정의할 수 있습니다(side-effects).

- `CreateTodo.vue`

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useMutation } from '@tanstack/vue-query'
import { createTodo } from '@/api/createTodo'

const title = ref('')

const { mutate, isPending } = useMutation({
  mutationFn: createTodo,
  onSuccess: () => {
    title.value = ''
  },
  onError: (error) => {
    console.error(error)
  },
})

const onClick = async () => {
  mutate({ title: title.value })
}
</script>

<template>
  <div>
    <input v-model="title" placeholder="할 일을 입력하세요" />
    <button @click="onClick" :disabled="isPending || !title">추가</button>
  </div>
</template>
```

투두 아이템을 생성하는 컴포넌트입니다. 버튼은 아직 요청이 진행 중이거나 내용이 없는 경우 클릭할 수 없습니다. 버튼을 누르면 `mutate()`를 통해 `mutationFn`으로 전달된 `createTodo()`를 호출합니다.  성공 시 다음 입력을 위해 `title` 값을 초기화하고, 실패 시 오류 로그를 남깁니다.

라우팅이 도입된 프로젝트의 경우 위 속성들을 이용해 성공/실패 시 다른 페이지로의 라우팅을 구현할 수 있습니다.

추가적인 내용은 [여기](https://tanstack.com/query/latest/docs/framework/vue/guides/mutations)를 참고하세요.

## Query Invalidation

하나의 컴포넌트에서 다른 컴포넌트의 데이터에 대해 특정 API를 재호출 하고 싶은 경우가 있을 수 있습니다. 예를 들어 투두 생성 폼과 투두리스트 표시 컴포넌트가 같은 페이지에 있는 경우, 새로운 투두 아이템을 생성했다면, 자동으로 투두리스트를 새로고침해야 합니다.

`QueryClient`의 `invalidateQueries()`를 이용하면 이를 구현할 수 있습니다.

- `App.vue`

```vue
<script setup lang="ts">
import CreateTodo from './components/CreateTodo.vue'
import TodoList from './components/TodoList.vue'
</script>

<template>
  <h1>Todo-List</h1>
  <CreateTodo />
  <TodoList />
</template>
```

한 페이지에서 생성을 위한 컴포넌트 `CreateTodo.vue`와 `TodoList.vue`가 모두 표시됩니다.

- `TodoList.vue`

```vue
<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { getTodos } from '@/api/getTodos'

const { data, isPending, isError, error } = useQuery({
  queryKey: ['todos'],
  queryFn: getTodos,
})
</script>

<template>
  <div>
    <span v-if="isPending">Loading...</span>
    <span v-else-if="isError">Error: {{ error?.message }}</span>
    <ul v-else-if="data">
      <li v-for="todo in data" :key="todo.id">{{ todo.title }}</li>
    </ul>
  </div>
</template>
```

`TodoList.vue` 컴포넌트는 기존과 같이 `useQuery`를 통해 데이터를 불러옵니다. `todos`를 Query Key로 갖습니다.

- `createTodo.ts`

```ts
import type { CreateTodoRequest } from '@/types/CreateTodoRequest'
import axios from 'axios'

export const createTodo = async (data: CreateTodoRequest) => {
  return (await axios.post('/api/todos', data)).data
}
```

- `CreateTodo.vue`

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { createTodo } from '@/api/createTodo'

const title = ref('')
const queryClient = useQueryClient()

const { mutate, isPending } = useMutation({
  mutationFn: createTodo,
  onSuccess: () => {
    title.value = ''
    queryClient.invalidateQueries({ queryKey: ['todos'] }) // <-- 추가
  },
  onError: (error) => {
    console.error(error)
  },
})

const onClick = async () => {
  mutate({ title: title.value })
}
</script>

<template>
  <div>
    <input v-model="title" placeholder="할 일을 입력하세요" />
    <button @click="onClick" :disabled="isPending || !title">추가</button>
  </div>
</template>
```

기존 `CreateTodo.vue`에 아래와 같이 코드 두 줄이 추가되었습니다.

```ts
const queryClient = useQueryClient()

...

queryClient.invalidateQueries({ queryKey: ['todos'] })
```

이를 통해 `CreateTodo.vue`가 `TodoList.vue` 등 다른 컴포넌트에서 `todos` 요청을 사용한다는 사실을 알지 못해도 `todos`를 현재 Query Key로 가지는 모든 API를 자동으로 재호출할 수 있습니다.

추가적인 내용은 [여기](https://tanstack.com/query/latest/docs/framework/vue/guides/query-invalidation)를 참고하세요.
