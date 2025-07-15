## Abstraction

해당 문서는 *KB IT’s Your Life*의 19반 2팀(계모임)에서 프론트엔드 개발 시 사용할 `TanStack Query` 라이브러리에 대해 통일성 있는 개발 컨벤션을 위한 가이드를 제공하기 위해 작성되었습니다.

`TanStack Query` 라이브러리에 대한 간략한 소개와 Vue에서 `useQuery`와 `useMutation`을 사용하는 방법을 예시와 함께 설명합니다.

해당 문서는 `TanStack Query` [공식 문서](https://tanstack.com/query/latest/docs/framework/vue/overview)를 참고하여 작성되었습니다. 해당 문서의 예시 일부는 `TypeScript`로 작성되었습니다. 

완성된 소스 코드를 [깃허브](https://github.com/gyehyun-bak/tanstack-query-vue-guide)에서 참조하실 수 있습니다.

## Overview

`TanStack Query`는 기존 프론트엔드 개발에서 데이터 페칭(data-fetching)과 관련된 로직을 다룰 때 필수적으로 구현해야 했던 다양한 상세 기능들을, 통일된 API로 제공하는 라이브러리입니다.

데이터를 단순히 가져오는 작업부터 캐싱, 동기화, 업데이트 등 여러 가지 다양한 서버와의 상호작용 관련 상황에서 사용할 수 있는 미리 만들어진 편리한 기능들을 제공합니다.

많은 기능이 있지만 개발 초기 단계에는 다음과 같은 기능들이 유용할 수 있습니다:

- 데이터 조회(useQuery)
- 데이터 업데이트(useMutation)
- 로딩 상태(isLoading, isPending)
- 성공 처리(isSuccess, onSuccess)
- 에러 처리(isError, onError)
- 새로 고침(refetch)
- 자동 재시도(retry)
- 페이지네이션(Paginated)
- 무한 스크롤(Infinite Queries)

모두 API 처리 관련해서 매번 등장하는 공통 관심사입니다. 이 모든 기능을 `try-catch`로 처리한다거나 개별 커스텀 훅을 만드는 것은 아주 번거로운 작업이 될 수 있습니다. `TanStack Query`는 이들을 간편하게 해결해주는 API를 제공합니다. 해당 문서에서는 이 중 몇 가지를 살펴볼 것입니다:

## Installation

NPM을 통해 Vue Query(TanStack Query)를 설치할 수 있습니다.

```bash
npm i @tanstack/vue-query
```

라이브러리가 설치되면 `VueQueryPlugin`의 초기화가 필요합니다. 다음과 같이 `main.ts` 파일을 수정합니다.

```tsx
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

```tsx
import { useQuery } from '@tanstack/vue-query'

const result = useQuery({ queryKey: ['todos'], queryFn: getTodos })
```

`useQuery`를 통해 반환되는 객체에는 다양한 프로퍼티가 포함되어 있습니다. 이들을 구조 분해 할당으로 가져올 수 있습니다.

```tsx
import { useQuery } from '@tanstack/vue-query'

const { isPending, isError, data, error } = useQuery({
  queryKey: ['todos'],
  queryFn: getTodos,
})
```

Todo 리스트 같은 단순한 서비스를 예시로 들겠습니다. 다음과 같이 API 호출 함수가 정의되어 있습니다.

- `getTodos.ts`

```tsx
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

`useQuery`를 통해 `isPending`, `isError`, `data`, `error`를 받아왔습니다. 각 데이터는 API가 요청 중인지, 성공했는지의 여부에 따라 자동으로 업데이트됩니다.

- `isPending`은 데이터를 아직 불러오기 전인지 표현합니다. 이를 활용하여 로딩 화면을 구성할 수 있습니다.
- `isError`는 비동기 함수가 에러를 반환했는지 여부를 표현합니다. 이를 활용하여 요청 에러 시 로직을 구성할 수 있습니다.
- `error`에는 에러가 반환된 경우 에러 객체가 할당됩니다. null 체크가 필요합니다.
- `data`는 `axios` 함수가 성공적으로 반환한 데이터입니다. null 체크가 필요합니다.

추가적인 내용은 [여기](https://tanstack.com/query/latest/docs/framework/vue/guides/queries)를 참고하세요.
