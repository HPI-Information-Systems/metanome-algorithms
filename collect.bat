if not exist _COLLECTION_ (
  mkdir _COLLECTION_
)

copy SPIDER\SPIDERFile\target\*.jar _COLLECTION_
copy SPIDER\SPIDERDatabase\target\*.jar _COLLECTION_

copy BINDER\BINDERFile\target\*.jar _COLLECTION_
copy BINDER\BINDERDatabase\target\*.jar _COLLECTION_

copy MANY\target\*.jar _COLLECTION_

copy HyFD\target\*.jar _COLLECTION_

copy HyUCC\target\*.jar _COLLECTION_

copy HyMD\metanome\target\*.jar _COLLECTION_

copy tane\tane_algorithm\target\*.jar _COLLECTION_
::copy tane\tane_tree_dir_algorithm\target\*.jar _COLLECTION_
::copy tane\tane_tree_end_algorithm\target\*.jar _COLLECTION_

copy fun\fun_for_metanome\target\*.jar _COLLECTION_

copy fdmine\fdmine_algorithm\target\*.jar _COLLECTION_

copy depminer\depminer_algorithm\target\*.jar _COLLECTION_

copy fastfds\fastfds_algorithm\target\*.jar _COLLECTION_

copy fdep\fdep_algorithm\target\*.jar _COLLECTION_
::copy fdep\fdep_algorithm_improved\target\*.jar _COLLECTION_

copy ducc\ducc_for_metanome\target\*.jar _COLLECTION_

copy dcucc\dcucc\target\*.jar _COLLECTION_

copy dfd\dfdMetanome\target\*.jar _COLLECTION_

copy ORDER\target\*.jar _COLLECTION_

copy AIDFD\target\*.jar _COLLECTION_

copy FAIDA\FAIDAAlgorithm\target\*.jar _COLLECTION_

copy SCDP\target\*.jar _COLLECTION_

copy MvdDet\target\*.jar _COLLECTION_

copy DVA\target\*.jar _COLLECTION_

copy DVAKMV\target\*.jar _COLLECTION_

copy DVAMS\target\*.jar _COLLECTION_

copy DVBJKST\target\*.jar _COLLECTION_

copy DVBloomFilter\target\*.jar _COLLECTION_

copy DVFM\target\*.jar _COLLECTION_

copy DVHyperLogLog\target\*.jar _COLLECTION_

copy DVHyperLogLogPlus\target\*.jar _COLLECTION_

copy DVLC\target\*.jar _COLLECTION_

copy DVLogLog\target\*.jar _COLLECTION_

copy DVMinCount\target\*.jar _COLLECTION_

copy DVPCSA\target\*.jar _COLLECTION_

copy DVSuperLogLog\target\*.jar _COLLECTION_

copy hydra\target\*.jar _COLLECTION_

copy CFDFinder\target\*.jar _COLLECTION_

copy Normalize\target\*.jar _COLLECTION_

copy tireless\target\*.jar _COLLECTION_

copy cody\cody-metanome\target\*.jar _COLLECTION_
