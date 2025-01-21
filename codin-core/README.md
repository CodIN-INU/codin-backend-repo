# CODIN BACKEND

## Server Status

- 


## 목차

---
- [모듈 트리 구조](#모듈-트리-구조)



## 모듈 트리 구조

```plaintext
├── common
│   ├── config
│   ├── exception
│   ├── ratelimit
│   ├── response
│   ├── security
│   │   ├── controller
│   │   ├── dto
│   │   ├── exception
│   │   ├── filter
│   │   ├── jwt
│   │   ├── service
│   │   └── util
│   └── util
├── domain
│   ├── chat
│   │   ├── chatroom
│   │   │   ├── controller
│   │   │   ├── dto
│   │   │   ├── entity
│   │   │   ├── exception
│   │   │   ├── repository
│   │   │   └── service
│   │   └── chatting
│   │       ├── controller
│   │       ├── dto
│   │       │   ├── request
│   │       │   └── response
│   │       ├── entity
│   │       ├── exception
│   │       ├── repository
│   │       └── service
│   ├── email
│   │   ├── controller
│   │   ├── dto
│   │   ├── entity
│   │   ├── exception
│   │   ├── repository
│   │   └── service
│   ├── info
│   │   ├── domain
│   │   │   ├── lab
│   │   │   │   ├── controller
│   │   │   │   ├── dto
│   │   │   │   │   ├── request
│   │   │   │   │   └── response
│   │   │   │   ├── entity
│   │   │   │   ├── exception
│   │   │   │   └── service
│   │   │   ├── office
│   │   │   │   ├── controller
│   │   │   │   ├── dto
│   │   │   │   │   ├── request
│   │   │   │   │   └── response
│   │   │   │   ├── entity
│   │   │   │   └── service
│   │   │   └── professor
│   │   │       ├── controller
│   │   │       ├── dto
│   │   │       │   ├── request
│   │   │       │   └── response
│   │   │       ├── entity
│   │   │       ├── exception
│   │   │       └── service
│   │   ├── entity
│   │   └── repository
│   ├── notification
│   │   ├── controller
│   │   └── dto
│   ├── post
│   │   ├── controller
│   │   ├── domain
│   │   │   ├── best
│   │   │   ├── comment
│   │   │   │   ├── controller
│   │   │   │   ├── dto
│   │   │   │   │   ├── request
│   │   │   │   │   └── response
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── hits
│   │   │   ├── like
│   │   │   │   ├── controller
│   │   │   │   ├── dto
│   │   │   │   ├── entity
│   │   │   │   ├── exception
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── poll
│   │   │   │   ├── controller
│   │   │   │   ├── dto
│   │   │   │   ├── entity
│   │   │   │   ├── exception
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   ├── reply
│   │   │   │   ├── controller
│   │   │   │   ├── dto
│   │   │   │   │   └── request
│   │   │   │   ├── entity
│   │   │   │   ├── repository
│   │   │   │   └── service
│   │   │   └── scrap
│   │   │       ├── controller
│   │   │       ├── entity
│   │   │       ├── exception
│   │   │       ├── repository
│   │   │       └── service
│   │   ├── dto
│   │   │   ├── request
│   │   │   └── response
│   │   ├── entity
│   │   ├── exception
│   │   ├── repository
│   │   └── service
│   └── user
│       ├── controller
│       ├── dto
│       │   ├── request
│       │   └── response
│       ├── entity
│       ├── exception
│       ├── repository
│       ├── security
│       └── service
└── infra
    ├── redis
    │   └── exception
    └── s3
        └── exception
```
