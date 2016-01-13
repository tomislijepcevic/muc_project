#!/system/bin/sh

# ---------------------------
# | CONFIGURATION VARIABLES |
# ---------------------------
BROAD_CAPTURE_DURATION=${1-10}
NARROW_CAPTURE_DURATION=${2-15}
FILE_OUI_MAPPING="oui.csv"

# ------------------------
# | FUNCTION DEFINITIONS |
# ------------------------
col_in_line() {
  line=$1
  column=$2
  echo $line | cut -d, -f$column
}

oui_lookup() {
  mac=$1
  mac_oui=$(echo $mac | cut -d: -f1,2,3)
  mac_nic=$(echo $mac | cut -d: -f4,5,6)
  mac_oui_resolved=$(grep -i $mac_oui oui.csv | cut -d, -f2)
  echo $mac_oui_resolved
}

print_client() {
  line=$1
  mac=$(col_in_line "$line" 1)
  pwr=$(col_in_line "$line" 4)
  bssid=$(col_in_line "$line" 6)
  mac_oui_resolved=$(oui_lookup $mac)
  echo $pwr,$mac,$mac_oui_resolved,$bssid
}

# -------------
# | MAIN CODE |
# -------------
# Start broad capture
file_broad_capture="broad-01.csv"

if test -e $file_broad_capture; then
  rm $file_broad_capture
fi

airodump-ng -w broad --output-format csv eth0 2>/dev/null &
sleep $BROAD_CAPTURE_DURATION
killall -INT airodump-ng
wait

# Process broad capture
dividing_line=$(grep -n "^Station" $file_broad_capture | cut -d: -f1)
num_of_lines=$(wc -l $file_broad_capture | cut -d' ' -f 1)
ap_from_line=2
ap_to_line=`expr $dividing_line - 2`
cl_from_line=$dividing_line
cl_to_line=`expr $num_of_lines - 1`

file_tmp="tmp.csv"
> $file_tmp
i=0

cat $file_broad_capture | while read line; do
  if [ $i -ge $ap_from_line -a $i -lt $ap_to_line ]; then
    mac=$(col_in_line "$line" 1)
    ch=$(col_in_line "$line" 4)
    pwr=$(col_in_line "$line" 9)

    echo $pwr,$mac,$ch >> $file_tmp
  elif [ $i -ge $cl_from_line -a $i -lt $cl_to_line ]; then
    print_client "$line"
  fi

  i=`expr $i + 1`
done

# Sort AP discovered in broad capture by power
$file_broad_capture > ble.txt
sort -nr $file_tmp > $file_broad_capture

# Start narrow capture
file_narrow_capture="narrow-01.csv"

if test -e $file_narrow_capture; then
  rm $file_narrow_capture
fi

cat $file_broad_capture | while read line; do
  mac=$(col_in_line "$line" 2)
  ch=$(col_in_line "$line" 3)

  airodump-ng -w narrow --output-format csv -c $ch --bssid $mac eth0 2>/dev/null &
  sleep $NARROW_CAPTURE_DURATION
  killall -INT airodump-ng
  wait

  dividing_line=$(grep -n "^Station" $file_narrow_capture | cut -d: -f1)
  num_of_lines=$(wc -l $file_narrow_capture | cut -d' ' -f 1)
  cl_from_line=$dividing_line
  cl_to_line=`expr $num_of_lines - 1`
  i=0

  cat $file_narrow_capture | while read _line; do
    if [ $i -ge $cl_from_line -a $i -lt $cl_to_line ]; then
      print_client "$_line"
    fi

    i=`expr $i + 1`
  done

  rm $file_narrow_capture
done

# rm $file_tmp
# rm $file_broad_capture
