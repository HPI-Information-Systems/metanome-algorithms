# CoDy

CoDy is a data profiling algorithm to discover Complementation Dependencies (CDs), a pattern of missing values, in relational datasets.

## Complementation Dependencies

A CD is a constraint that states that two sets of attributes are never jointly observed, i.e., records that have values in the first set of attributes do not have any value in the second set of attributes, and vice versa. Thus, they complement each other, forming a pattern of missing data.

_Example._ A CD between {W, L} and {⌀} exists in the table below:

| Item    |   W  |   L  |  H  |   ⌀  |
|---------|:----:|:----:|:---:|:----:|
| Bread   |  8.0 | 15.0 | 8.5 |      |
| Cookie  |      |      | 1.5 |  5.0 |
| Bagel   |      |      | 4.5 | 10.0 |
| Brownie | 10.0 |  5.5 | 2.5 |      |
| Cake    |      |      | 8.0 | 28.0 |

### Use Case

Missing data is a prevalent problem in real-world datasets. Knowing how missing values occur is crucial to understanding a dataset and applying appropriate strategies to handle them. However, for most datasets such metadata is unknown. Discovering CDs helps data scientists and analysts better understand datasets, impute missing values, and define data quality rules.


## Algorithm

CoDy is an algorithm to discover CDs and their approximate variant in relational datasets. It employs position list indices and deduplication for fast candidate validation and a bottom-up top-down lattice traversal strategy to efficiently search the candidate space. CoDy can process even large datasets efficiently. Compared to existing approaches, it is faster by orders of magnitude.


```tex
@mastersthesis{cody,
  author    = {Jonas Hering}, 
  title     = {Discovery of Complementation Dependencies},
  school    = {Hasso Plattner Institute, University of Potsdam},
  year      = 2022,
  address   = {Potsdam, Germany},
  month     = 5
}
```
