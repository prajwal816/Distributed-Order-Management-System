# System Architecture

## High-Level Architecture

```
                            ┌─────────────────┐
                            │   Client Apps    │
                            │  (Web/Mobile)    │
                            └────────┬────────┘
                                     │ HTTPS
                            ┌────────▼────────┐
                            │   API Gateway    │
                            │   (Port 8080)    │
                            │  ┌────────────┐  │
                            │  │ JWT Auth    │  │
                            │  │ Rate Limit  │  │
                            │  │ Logging     │  │
                            │  │ Metrics     │  │
                            │  └────────────┘  │
                            └──┬──┬──┬──┬──────┘
              ┌────────────────┘  │  │  └────────────────┐
              │         ┌────────┘  └────────┐           │
     ┌────────▼───────┐ │  ┌────────────────┐│ ┌────────▼───────┐
     │Product Service │ │  │  Cart Service  ││ │  Inventory     │
     │  (Port 8081)   │ │  │  (Port 8082)   ││ │  Service       │
     │                │ │  │                ││ │  (Port 8084)   │
     │ ┌────────────┐ │ │  │ ┌────────────┐ ││ │ ┌────────────┐ │
     │ │ Strategy   │ │ │  │ │ Circuit    │ ││ │ │ Optimistic │ │
     │ │ Pattern    │ │ │  │ │ Breaker    │ ││ │ │ Locking    │ │
     │ └────────────┘ │ │  │ └────────────┘ ││ │ └────────────┘ │
     └────────┬───────┘ │  └───────┬────────┘│ └────────┬───────┘
              │         │          │          │          │
              │    ┌────▼──────────▼───┐      │          │
              │    │  Order Service    │      │          │
              │    │   (Port 8083)     │◄─────┘          │
              │    │                   │                 │
              │    │ ┌──────────────┐  │                 │
              │    │ │Factory Ptrn  │  │                 │
              │    │ │Observer Ptrn │  │                 │
              │    │ └──────────────┘  │                 │
              │    └────────┬──────────┘                 │
              │             │                            │
     ┌────────▼─────────────▼────────────────────────────▼───┐
     │                    PostgreSQL 16                       │
     │              (Products, Carts, Orders, Inventory)     │
     └───────────────────────────────────────────────────────┘
     ┌───────────────────────────────────────────────────────┐
     │                     Redis 7.2                         │
     │           (Product Cache, Cart Sessions,              │
     │            Inventory Cache)                           │
     └───────────────────────────────────────────────────────┘
```

## Communication Flow

1. **Client → Gateway**: All requests enter through the API Gateway (port 8080)
2. **Gateway → Services**: Routes are resolved by path prefix (`/api/products` → Product Service)
3. **Inter-service**: Services call each other via REST with Resilience4j circuit breakers
4. **Service → DB**: Each service connects to PostgreSQL via connection pool (HikariCP)
5. **Service → Cache**: Read-through caching with Redis, automatic TTL-based eviction

## Design Patterns

### Strategy Pattern (Product Service)
- `ProductSearchStrategy` interface with `NameSearchStrategy`, `CategorySearchStrategy`, `PriceRangeSearchStrategy`
- Selected at runtime based on `searchType` query parameter

### Factory Pattern (Order Service)
- `OrderFactory` creates orders with pricing logic dependent on `OrderType`
- STANDARD: base price, EXPRESS: +15% surcharge, BULK: -10% discount (5+ items)

### Observer Pattern (Order Service)
- `OrderEventPublisher` dispatches lifecycle events to registered listeners
- `InventoryUpdateListener`: reserves/releases stock
- `NotificationListener`: simulates email/SMS notifications
