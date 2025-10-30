<template>
  <div class="map-container">
    <auto-complete
      v-show="showMapTable"
      v-model="searchAddress"
      placeholder="请输入详细地址"
      style="width: 100%; margin-bottom: 10px;"
      @search="onSearch"
      @select="onSelect"
    >
      <template slot="dataSource">
        <a-select-option v-for="item in dataSource" :key="item">
          {{ item }}
        </a-select-option>
      </template>
    </auto-complete>

    <div :id="`container_${_uid}`" class="a-map-box"></div>

    <div class="address-view">
      <div style="width: 100%">
        <label>坐标：</label>
        <a-input :value="locationStr" style="width: 300px" :disabled="true"/>
      </div>
    </div>
  </div>
</template>

<script>
import AMapLoader from '@amap/amap-jsapi-loader'
import {AutoComplete} from 'ant-design-vue'

export default {
  components: {AutoComplete},
  props: {
    lnglat: {
      type: Object,
      default: () => ({})
    },
    zoom: {
      type: Number,
      default: 10
    },
    showMapTable: {
      type: Boolean,
      default: true
    },
    disableClick: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      map: null,
      searchAddress: '',
      location: {
        lat: 44.61081,
        lng: 80.990372
      },
      dataSource: [],
      allSource: []
    }
  },
  computed: {
    locationStr() {
      return this.location.lng + ',' + this.location.lat
    }
  },
  created() {
    if (this.lnglat.lng && this.lnglat.lat) {
      this.location = this.lnglat
    }
    AMapLoader.load({
      key: 'ebeb9911ac6faf1c7269809bb6ba03c4', // 申请好的Web端开发者Key，首次调用 load 时必填
      version: '2.0', // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
      plugins: ['AMap.Geocoder', 'AMap.AutoComplete'] // 需要使用的的插件列表，如比例尺'AMap.Scale'等
    })
    .then((AMap) => {
      window.AMap = AMap
      this.map = new AMap.Map(`container_${this._uid}`, {
        zoom: this.zoom,
        center: [110.291848, 35.448023]
      })

      this.geocoder = new AMap.Geocoder()
      this.autoComplete = new AMap.AutoComplete()

      this.drawPoint()
      this.map.panTo([this.location.lng, this.location.lat])

      if (this.disableClick) {
        return
      }
      this.map.on('click', (e) => {
        this.location = {
          lng: e.lnglat.lng,
          lat: e.lnglat.lat
        }
        this.drawPoint()
      })
    })
    .catch((e) => {
      console.log(e)
    })
  },
  methods: {
    drawPoint() {
      /* eslint-disable */

      // 添加之前先移除
      if (this.marker) {
        this.map.remove(this.marker)
      }

      this.marker = new AMap.Marker({
        position: new AMap.LngLat(this.location.lng, this.location.lat),
      })
      this.map.add(this.marker)

      /* eslint-disable */
    },
    onSelect(name) {
      const row = this.allSource.filter(t => t.value === name)[0]
      this.searchAddress = name

      if (row.location) {
        this.location = row.location
        this.drawPoint()
        this.map.panTo([this.location.lng, this.location.lat])
      } else {
        const value = row.value.split('-')[0]
        this.geocoder.getLocation(row.value, (status, result) => {
          if (status === 'complete') {
            this.location = result.geocodes[0].location
            this.drawPoint()
            this.map.panTo([this.location.lng, this.location.lat])
          } else {
            this.$message.error('不存在该地址')
          }
        })
      }
    },
    onSearch(queryString) {

      this.autoComplete.search(queryString, (status, result) => {
        if (status === 'complete') {
          const valueList = []
          const list = []
          result.tips.forEach((item) => {
            list.push({
              value: item.name + '-' + item.district + item.address,
              location: item.location,
            })
            valueList.push(item.name + '-' + item.district + item.address)
          })
          this.dataSource = valueList
          this.allSource = list
        } else if (status === 'error') {
          this.dataSource = []
          this.allSource = []
        }
      })
    },
  },
}
</script>

<style scoped lang="less">
.map-container {
  width: 100%;
  height: 100%;
}

.a-map-box {
  width: 100%;
  height: calc(100% - 90px);
}

.address-view {
  margin-top: 20px;
  text-align: center;
}
</style>
