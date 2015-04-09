if not exist _COLLECTION_ (
  mkdir _COLLECTION_
)

copy SPIDER\SPIDERFile\target\*.jar _COLLECTION_
copy SPIDER\SPIDERDatabase\target\*.jar _COLLECTION_

copy BINDER\BINDERFile\target\*.jar _COLLECTION_
copy BINDER\BINDERDatabase\target\*.jar _COLLECTION_

copy anelosimus\target\*.jar _COLLECTION_

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

copy dfd\dfdMetanome\target\*.jar _COLLECTION_

copy ORDER\target\*.jar _COLLECTION_
