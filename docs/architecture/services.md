# Service Map

Rules of the road
1. The **API Gateway** is the only public entry point.
2. A service **owns its data**. No other service reads its database - ask through its API or events.
3. Calls are synchronous REST first. Events (Kafka/RabbitMQ) are introduced only where a phase needs them.
4. Money flows (order -> payment -> payout) use a saga with compensation (Phase 4).

| Service | Owns | Phase |
|---|---|---|
| auth-service | accounts, roles, credentials, tokens | 1 |
| seller-verification-service | verification requests, documents, decisions | 3 |
| catalog-service | books, listings (new / second-hand / rent), condition grade, reservation state | 2 |
| inventory-service | physical copies, location, custody history | 2 |
| negotiation-service | offers, counter-offers, agreed price | 5 |
| order-service | orders, order lines, fulfilment choice | 4 |
| payment-service | payments, commission, seller/owner earnings, payouts | 4 |
| membership-service | plans, subscriptions, allowance usage | 6 |
| rental-service | rentals, due dates, returns, fines, consignment terms | 6 |
| delivery-service | riders, pickup/delivery tasks, daily rider reports | 7 |
| review-service | satisfaction and book reviews | 7 |
| notification-service | email and in-app messages | 7 |

Infrastructure: discovery-server (Eureka), config-server, api-gateway (Spring Cloud Gateway).
