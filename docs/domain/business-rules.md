# Business Rules (source of truth)

Every rule here came from the product owner. Code and tests must trace back to a rule ID.
Items marked **TBD** are decisions still open.

## Actors
- **User** - can buy, sell second-hand, rent out and rent books.
- **Verified Seller** - a user approved to sell new books.
- **Admin (Book Corner Nepal)** - runs the platform and also sells/rents its own books.
- **Rider** - Book Corner's own delivery person.

## Selling
- BR-01 Any user can sell books as **second-hand**.
- BR-02 Only **verified sellers** (and Book Corner itself) can sell **new** books.
- BR-03 Verification: the seller submits business documents; an admin approves or rejects.
- BR-04 Book Corner lists its own books for new sale, second-hand sale and rent.

## Condition
- BR-05 Every second-hand and rental book has a condition grade: **Like New, Good, Fair, Poor**.
- BR-06 *(design decision)* Staff inspect second-hand and rental books at intake and confirm the grade.

## Negotiation (second-hand only)
- BR-07 A buyer starts a negotiation on a second-hand listing; buyer and seller negotiate directly.
- BR-08 While a negotiation is active the listing is **reserved** for that buyer.
- BR-09 When both agree, the seller requests the price change and the admin **records** the agreed price (admin does not change it).
- BR-10 After that, the order proceeds as a normal order.

## Fees and payments
- BR-11 Commission is **2%**, charged to the seller on sales and to the book owner on rent earnings.
- BR-12 All payments are collected on the platform (eSewa, Khalti).
- BR-13 The seller/owner is paid out after Book Corner has received the payment; there is no waiting window.
  *Design note: with no hold window, a refund after payout is a loss for the platform. Revisit before launch.*

## Membership and renting
- BR-14 Plans: **1, 3, 6 and 12 months**; each has a price and a book allowance. Membership usage is tracked.
- BR-15 1-month plan: a member may hold **3 books at a time**. After returning books while the membership is still active, they may rent again - total rentals are not capped at 3.
- BR-16 Renting a **Book Corner** book requires an active membership.
- BR-17 Members get a **discount on new books** (percentage TBD).
- BR-18 **User-to-user rental** needs no membership: the owner sets a per-day price and the renter pays price x days kept.
- BR-19 **Consignment**: a user can give a book to Book Corner to rent out for **1, 2 or 3 months**. The owner sets the price; Book Corner collects the book, stores it, rents it out, and pays the owner the rent earned (minus 2%).
- BR-20 Everything is tracked: who owns each book, where it is, whether it is rented, to whom, until when.

## Fines
- BR-21 Late fine: **Rs 5 per day** after the due date.
- BR-22 Damage fee depends on the condition grade (amounts **TBD**).

## Fulfilment
- BR-23 Sellers either drop the book at the store or request pickup by Book Corner.
- BR-24 Buyers either pick up from the store or choose delivery.
- BR-25 Book Corner delivers with its own riders (new, second-hand, rental). Riders have accounts; daily deliveries are tracked, including whether the book was sold by the store or a seller.
- BR-26 A third-party delivery partner for verified sellers may come later; not now.

## Reviews
- BR-27 Only users who bought or rented a book can review it.
- BR-28 Two review types per purchase: a **satisfaction review** (experience with the seller/transaction) and a **book review** (the book itself). A user may leave both.

## Accounts
- BR-29 Users log in with email and password. Accounts are active immediately after registration.
- BR-30 A user must have a **verified email** before placing an order, listing a book, or renting. Browsing and
  logging in do not require it. *(Rule enforced once the verification flow exists, Phase 7.)*

## Open questions
- Membership plan prices and allowances for 3/6/12 months (TBD)
- Discount percentage on new books for members (TBD)
- Damage fee amount per condition grade (TBD)
- Delivery fee rules, by location (TBD)
